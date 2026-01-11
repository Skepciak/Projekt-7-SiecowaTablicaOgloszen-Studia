package pl.tablicaogloszen.klient;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import pl.tablicaogloszen.wspolne.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class KontrolerTablicy {

    @FXML
    private FlowPane kontenerOgloszen; // FlowPane dla siatki pergaminów

    @FXML
    private ComboBox<String> filtrKategoria;
    @FXML
    private TextField filtrAutor;
    @FXML
    private TextField filtrTekst;
    @FXML
    private ComboBox<String> filtrSortowanie;
    @FXML
    private Label etykietaRola;

    private final Random random = new Random();

    // Lista kategorii do użycia w dialogach
    private List<String> listaKategorii;

    public void initialize() {
        pobierzKategorie();
        filtrSortowanie.setItems(FXCollections.observableArrayList(
                "Najnowsze", "Najstarsze", "Tytuł A-Z", "Tytuł Z-A"));
        filtrSortowanie.setValue("Najnowsze");

        UzytkownikDTO uzytkownik = Sesja.getZalogowanyUzytkownik();
        if (uzytkownik != null) {
            boolean czyAdmin = "ADMIN".equals(uzytkownik.getRola());
            etykietaRola.setText(czyAdmin ? "👑 ADMIN" : "👤 " + uzytkownik.getLogin());
        }

        KlientSieciowy.pobierzInstancje().ustawKontrolerTablicy(this);
        odswiezListe();
    }

    /**
     * Tworzy kartę pergaminową (małą karteczkę przypiętą do tablicy)
     */
    private VBox utworzKarteOgloszenia(OgloszenieDTO item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("contract-note");
        card.setPrefWidth(280);
        card.setMinHeight(220);

        // Losowa rotacja dla naturalnego efektu
        double angle = random.nextDouble() * 4 - 2; // -2 do 2 stopni
        card.setRotate(angle);

        // Ikona Pinezki
        Label pin = new Label("📍");
        pin.getStyleClass().add("pin-icon");

        // Nagłówek (Tytuł)
        Label titleLabel = new Label(item.getTytul());
        titleLabel.getStyleClass().add("contract-title");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        // Kategoria i Data
        HBox metaBox = new HBox(10);
        metaBox.setAlignment(Pos.CENTER);

        Label catLabel = new Label(item.getKategoria());
        catLabel.getStyleClass().add("ad-category");

        String dataStr = item.getDataDodania() != null
                ? item.getDataDodania().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                : "Teraz";
        Label dateLabel = new Label("📅 " + dataStr);
        dateLabel.getStyleClass().add("ad-meta");

        metaBox.getChildren().addAll(catLabel, dateLabel);

        // Treść
        Label contentLabel = new Label(item.getTresc());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("ad-content");
        contentLabel.setMaxHeight(80);
        VBox.setVgrow(contentLabel, Priority.ALWAYS);

        // Kontakt
        HBox kontaktBox = new HBox(5);
        kontaktBox.setAlignment(Pos.CENTER_LEFT);
        if (item.getDaneKontaktowe() != null && !item.getDaneKontaktowe().isEmpty()) {
            Label kontaktLabel = new Label("📞 " + item.getDaneKontaktowe());
            kontaktLabel.getStyleClass().add("ad-meta");
            kontaktLabel.setStyle("-fx-font-size: 11px;");
            kontaktBox.getChildren().add(kontaktLabel);
        }

        Separator separator = new Separator();

        // Stopka (Autor + Przyciski)
        HBox footer = new HBox(5);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label authorLabel = new Label("👤 " + item.getAutor());
        authorLabel.getStyleClass().add("ad-meta");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footer.getChildren().addAll(authorLabel, spacer);

        // Przyciski edycji/usuwania
        UzytkownikDTO zalogowany = Sesja.getZalogowanyUzytkownik();
        if (zalogowany != null) {
            boolean czyAdmin = "ADMIN".equals(zalogowany.getRola());
            boolean czyWlasciciel = item.getIdAutora() == zalogowany.getId();

            if (czyWlasciciel || czyAdmin) {
                Button btnEdytuj = new Button("📝");
                btnEdytuj.getStyleClass().addAll("action-icon-button", "edit-button");
                btnEdytuj.setTooltip(new Tooltip("Edytuj"));
                btnEdytuj.setOnAction(e -> edytujOgloszenie(item));

                Button btnUsun = new Button("❌");
                btnUsun.getStyleClass().addAll("action-icon-button", "delete-button");
                btnUsun.setTooltip(new Tooltip("Usuń"));
                btnUsun.setOnAction(e -> usunOgloszenie(item));

                footer.getChildren().addAll(btnEdytuj, btnUsun);
            }
        }

        // Składanie całości
        card.getChildren().addAll(pin, titleLabel, metaBox, contentLabel, kontaktBox, separator, footer);
        return card;
    }

    private void pobierzKategorie() {
        new Thread(() -> {
            Zadanie zadanie = new Zadanie(TypZadania.POBIERZ_KATEGORIE, null);
            Odpowiedz odp = KlientSieciowy.pobierzInstancje().wyslijISprawdz(zadanie);
            if (odp != null && odp.getStatus() == StatusOdpowiedzi.OK) {
                @SuppressWarnings("unchecked")
                List<String> kategorie = (List<String>) odp.getDane();
                listaKategorii = kategorie;
                Platform.runLater(() -> {
                    filtrKategoria.getItems().clear();
                    filtrKategoria.getItems().add("Wszystkie");
                    filtrKategoria.getItems().addAll(kategorie);
                });
            }
        }).start();
    }

    @FXML
    public void odswiezListe() {
        zastosujFiltr();
    }

    @FXML
    private void zastosujFiltr() {
        String kategoria = filtrKategoria.getValue();
        if ("Wszystkie".equals(kategoria))
            kategoria = null;

        String autor = filtrAutor.getText().trim();
        if (autor.isEmpty())
            autor = null;

        String tekst = filtrTekst.getText().trim();
        if (tekst.isEmpty())
            tekst = null;

        String sortowanie;
        if (filtrSortowanie.getValue() != null) {
            switch (filtrSortowanie.getValue()) {
                case "Najstarsze":
                    sortowanie = "DATA_ASC";
                    break;
                case "Tytuł A-Z":
                    sortowanie = "TYTUL_ASC";
                    break;
                case "Tytuł Z-A":
                    sortowanie = "TYTUL_DESC";
                    break;
                default:
                    sortowanie = "DATA_DESC";
                    break;
            }
        } else {
            sortowanie = "DATA_DESC";
        }

        FiltrDTO filtr = new FiltrDTO(kategoria, autor, tekst, sortowanie);
        Zadanie zadanie = new Zadanie(TypZadania.POBIERZ_OGLOSZENIA_FILTR, filtr);

        new Thread(() -> {
            Odpowiedz odp = KlientSieciowy.pobierzInstancje().wyslijISprawdz(zadanie);
            Platform.runLater(() -> {
                kontenerOgloszen.getChildren().clear();
                if (odp != null) {
                    if (odp.getStatus() == StatusOdpowiedzi.OK) {
                        @SuppressWarnings("unchecked")
                        List<OgloszenieDTO> ogloszenia = (List<OgloszenieDTO>) odp.getDane();
                        for (OgloszenieDTO ogl : ogloszenia) {
                            kontenerOgloszen.getChildren().add(utworzKarteOgloszenia(ogl));
                        }
                    } else {
                        utworzAlert(Alert.AlertType.ERROR, "Błąd pobierania: " + odp.getWiadomosc());
                    }
                } else {
                    utworzAlert(Alert.AlertType.ERROR, "Brak odpowiedzi od serwera.");
                }
            });
        }).start();
    }

    @FXML
    private void wyczyscFiltr() {
        filtrKategoria.setValue(null);
        filtrAutor.clear();
        filtrTekst.clear();
        filtrSortowanie.setValue("Najnowsze");
        zastosujFiltr();
    }

    /**
     * Otwiera okno dialogowe do dodawania nowego ogłoszenia
     */
    @FXML
    private void pokazDialogDodawania() {
        Dialog<OgloszenieDTO> dialog = new Dialog<>();
        dialog.setTitle("Nowe Zlecenie");
        dialog.setHeaderText("📜 Przypnij nowe ogłoszenie do tablicy");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("witcher-dialog");
        dialog.getDialogPane().setPrefWidth(500);

        ButtonType przypnijButton = new ButtonType("⚔ Przypnij", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(przypnijButton, ButtonType.CANCEL);

        // Zawartość dialogu w stylu pergaminu
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("ad-card");

        // Tytuł
        VBox tytulBox = new VBox(5);
        Label lblTytul = new Label("Tytuł zlecenia:");
        lblTytul.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b1d0e;");
        TextField poleTytul = new TextField();
        poleTytul.setPromptText("Np. Kontrakt: Gryf w okolicy...");
        poleTytul.setPrefHeight(40);
        tytulBox.getChildren().addAll(lblTytul, poleTytul);

        // Kategoria
        VBox kategoriaBox = new VBox(5);
        Label lblKategoria = new Label("Kategoria:");
        lblKategoria.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b1d0e;");
        ComboBox<String> comboKategoria = new ComboBox<>();
        if (listaKategorii != null) {
            comboKategoria.setItems(FXCollections.observableArrayList(listaKategorii));
        }
        comboKategoria.setPromptText("Wybierz kategorię");
        comboKategoria.setMaxWidth(Double.MAX_VALUE);
        comboKategoria.setPrefHeight(40);
        kategoriaBox.getChildren().addAll(lblKategoria, comboKategoria);

        // Dane kontaktowe
        VBox kontaktBox = new VBox(5);
        Label lblKontakt = new Label("Dane kontaktowe:");
        lblKontakt.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b1d0e;");
        TextField poleKontakt = new TextField();
        poleKontakt.setPromptText("Karczma, imię, lokalizacja...");
        poleKontakt.setPrefHeight(40);
        kontaktBox.getChildren().addAll(lblKontakt, poleKontakt);

        // Treść
        VBox trescBox = new VBox(5);
        Label lblTresc = new Label("Treść ogłoszenia:");
        lblTresc.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b1d0e;");
        TextArea poleTresc = new TextArea();
        poleTresc.setPromptText("Opisz szczegóły zlecenia, nagrodę, niebezpieczeństwo...");
        poleTresc.setWrapText(true);
        poleTresc.setPrefRowCount(5);
        trescBox.getChildren().addAll(lblTresc, poleTresc);

        content.getChildren().addAll(tytulBox, kategoriaBox, kontaktBox, trescBox);
        dialog.getDialogPane().setContent(content);

        // Fokus na pierwszym polu
        Platform.runLater(() -> poleTytul.requestFocus());

        dialog.setResultConverter(button -> {
            if (button == przypnijButton) {
                String tytul = poleTytul.getText().trim();
                String kategoria = comboKategoria.getValue();

                if (tytul.isEmpty() || kategoria == null) {
                    utworzAlert(Alert.AlertType.WARNING, "Uzupełnij tytuł i kategorię!");
                    return null;
                }

                return new OgloszenieDTO(
                        tytul,
                        poleTresc.getText().trim(),
                        poleKontakt.getText().trim(),
                        kategoria,
                        Sesja.getZalogowanyUzytkownik().getLogin());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nowe -> {
            new Thread(() -> {
                Zadanie zadanie = new Zadanie(TypZadania.DODAJ_OGLOSZENIE, nowe);
                Odpowiedz odp = KlientSieciowy.pobierzInstancje().wyslijISprawdz(zadanie);
                Platform.runLater(() -> {
                    if (odp != null && odp.getStatus() == StatusOdpowiedzi.OK) {
                        utworzAlert(Alert.AlertType.INFORMATION, "Ogłoszenie przypięte do tablicy!");
                        odswiezListe();
                    } else {
                        utworzAlert(Alert.AlertType.ERROR,
                                "Błąd dodawania: " + (odp != null ? odp.getWiadomosc() : "brak odpowiedzi"));
                    }
                });
            }).start();
        });
    }

    @FXML
    private void wyloguj() {
        Sesja.wyloguj();
        try {
            AplikacjaKlienta.zaladujWidok("logowanie");
        } catch (Exception e) {
            utworzAlert(Alert.AlertType.ERROR, "Błąd wylogowywania: " + e.getMessage());
        }
    }

    private void edytujOgloszenie(OgloszenieDTO item) {
        Dialog<OgloszenieDTO> dialog = new Dialog<>();
        dialog.setTitle("Edycja");
        dialog.setHeaderText("📝 Edytuj ogłoszenie");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("witcher-dialog");
        dialog.getDialogPane().setPrefWidth(500);

        ButtonType zapiszButton = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(zapiszButton, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("ad-card");

        TextField edTytul = new TextField(item.getTytul());
        edTytul.setPrefHeight(40);

        TextArea edTresc = new TextArea(item.getTresc());
        edTresc.setWrapText(true);
        edTresc.setPrefRowCount(5);

        TextField edKontakt = new TextField(item.getDaneKontaktowe());
        edKontakt.setPrefHeight(40);

        ComboBox<String> edKategoria = new ComboBox<>();
        if (listaKategorii != null) {
            edKategoria.setItems(FXCollections.observableArrayList(listaKategorii));
        }
        edKategoria.setValue(item.getKategoria());
        edKategoria.setMaxWidth(Double.MAX_VALUE);
        edKategoria.setPrefHeight(40);

        Label lbl1 = new Label("Tytuł:");
        lbl1.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b1d0e;");
        Label lbl2 = new Label("Kategoria:");
        lbl2.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b1d0e;");
        Label lbl3 = new Label("Kontakt:");
        lbl3.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b1d0e;");
        Label lbl4 = new Label("Treść:");
        lbl4.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b1d0e;");

        content.getChildren().addAll(lbl1, edTytul, lbl2, edKategoria, lbl3, edKontakt, lbl4, edTresc);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(b -> {
            if (b == zapiszButton) {
                item.setTytul(edTytul.getText());
                item.setTresc(edTresc.getText());
                item.setDaneKontaktowe(edKontakt.getText());
                item.setKategoria(edKategoria.getValue());
                return item;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(edycja -> {
            new Thread(() -> {
                Odpowiedz odp = KlientSieciowy.pobierzInstancje()
                        .wyslijISprawdz(new Zadanie(TypZadania.EDYTUJ_OGLOSZENIE, edycja));
                Platform.runLater(() -> {
                    if (odp != null && odp.getStatus() == StatusOdpowiedzi.OK) {
                        utworzAlert(Alert.AlertType.INFORMATION, "Zapisano zmiany.");
                        odswiezListe();
                    } else {
                        utworzAlert(Alert.AlertType.ERROR,
                                "Błąd edycji: " + (odp != null ? odp.getWiadomosc() : "brak odp"));
                    }
                });
            }).start();
        });
    }

    private void usunOgloszenie(OgloszenieDTO item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potwierdzenie");
        confirm.setHeaderText("Usunąć '" + item.getTytul() + "'?");
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        confirm.getDialogPane().getStyleClass().add("witcher-dialog");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    Odpowiedz odp = KlientSieciowy.pobierzInstancje()
                            .wyslijISprawdz(new Zadanie(TypZadania.USUN_OGLOSZENIE, item.getId()));
                    Platform.runLater(() -> {
                        if (odp != null && odp.getStatus() == StatusOdpowiedzi.OK) {
                            utworzAlert(Alert.AlertType.INFORMATION, "Usunięto.");
                            odswiezListe();
                        } else {
                            utworzAlert(Alert.AlertType.ERROR,
                                    "Błąd usuwania: " + (odp != null ? odp.getWiadomosc() : "brak odp"));
                        }
                    });
                }).start();
            }
        });
    }

    @FXML
    private void generujRaport() {
        // TODO: Implementacja raportu
        utworzAlert(Alert.AlertType.INFORMATION, "Funkcja raportów w przygotowaniu...");
    }

    private void utworzAlert(Alert.AlertType typ, String tresc) {
        Alert alert = new Alert(typ);
        alert.setHeaderText(null);
        alert.setContentText(tresc);
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("witcher-dialog");
        } catch (Exception e) {
        }
        alert.showAndWait();
    }
}
