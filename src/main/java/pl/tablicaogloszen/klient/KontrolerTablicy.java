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

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import javafx.stage.FileChooser;

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
    @FXML
    private Button przyciskZgloszone;

    private final Random random = new Random();

    // Lista kategorii do użycia w dialogach
    private List<String> listaKategorii;

    public void initialize() {
        pobierzKategorie();
        filtrSortowanie.setItems(FXCollections.observableArrayList(
                "Najnowsze", "Najstarsze", "Tytuł A-Z", "Tytuł Z-A", "Najpopularniejsze"));
        filtrSortowanie.setValue("Najnowsze");

        UzytkownikDTO uzytkownik = Sesja.getZalogowanyUzytkownik();
        if (uzytkownik != null) {
            boolean czyAdmin = "ADMIN".equals(uzytkownik.getRola());
            etykietaRola.setText(czyAdmin ? "👑 ADMIN" : "👤 " + uzytkownik.getLogin());
            // Przycisk zgłoszonych widoczny TYLKO dla admina
            przyciskZgloszone.setVisible(czyAdmin);
            przyciskZgloszone.setManaged(czyAdmin);
        } else {
            przyciskZgloszone.setVisible(false);
            przyciskZgloszone.setManaged(false);
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

        // Licznik popularności (wyświetleń)
        Label viewsLabel = new Label("👁 " + item.getWyswietlenia());
        viewsLabel.getStyleClass().add("ad-meta");
        viewsLabel.setTooltip(new Tooltip("Liczba wyświetleń"));
        metaBox.getChildren().add(viewsLabel);

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

            // Przycisk zgłaszania (dla zalogowanych, którzy nie są właścicielami)
            if (!czyWlasciciel && !czyAdmin) {
                Button btnZglos = new Button("🚩");
                btnZglos.getStyleClass().addAll("action-icon-button");
                btnZglos.setTooltip(new Tooltip("Zgłoś nieodpowiednie ogłoszenie"));
                btnZglos.setOnAction(e -> zglosOgloszenie(item));
                footer.getChildren().add(btnZglos);
            }
        }

        // Kliknięcie na kartę otwiera szczegóły
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                pokazSzczegoly(item);
            }
        });

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
                case "Najpopularniejsze":
                    sortowanie = "POPULARNOSC_DESC";
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
        UzytkownikDTO zalogowany = Sesja.getZalogowanyUzytkownik();
        if (zalogowany == null || !"ADMIN".equals(zalogowany.getRola())) {
            utworzAlert(Alert.AlertType.WARNING, "Tylko administrator może generować raporty.");
            return;
        }

        new Thread(() -> {
            Zadanie zadanie = new Zadanie(TypZadania.GENERUJ_RAPORT, null);
            Odpowiedz odp = KlientSieciowy.pobierzInstancje().wyslijISprawdz(zadanie);

            Platform.runLater(() -> {
                if (odp != null && odp.getStatus() == StatusOdpowiedzi.OK) {
                    String raport = (String) odp.getDane();

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Zapisz raport");
                    fileChooser.setInitialFileName("raport_ogloszenia.txt");
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt"));

                    java.io.File file = fileChooser.showSaveDialog(kontenerOgloszen.getScene().getWindow());
                    if (file != null) {
                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write(raport);
                            utworzAlert(Alert.AlertType.INFORMATION, "Raport zapisany: " + file.getName());
                        } catch (IOException e) {
                            utworzAlert(Alert.AlertType.ERROR, "Błąd zapisu: " + e.getMessage());
                        }
                    }
                } else {
                    utworzAlert(Alert.AlertType.ERROR, "Błąd generowania raportu: " +
                            (odp != null ? odp.getWiadomosc() : "brak odpowiedzi"));
                }
            });
        }).start();
    }

    /**
     * Wyświetla dialog ze zgłoszonymi ogłoszeniami (tylko dla admina)
     */
    @FXML
    private void pokazZgloszone() {
        new Thread(() -> {
            Zadanie zadanie = new Zadanie(TypZadania.POBIERZ_ZGLOSZONE, null);
            Odpowiedz odp = KlientSieciowy.pobierzInstancje().wyslijISprawdz(zadanie);

            Platform.runLater(() -> {
                if (odp != null && odp.getStatus() == StatusOdpowiedzi.OK) {
                    @SuppressWarnings("unchecked")
                    List<OgloszenieDTO> zgloszone = (List<OgloszenieDTO>) odp.getDane();

                    if (zgloszone.isEmpty()) {
                        utworzAlert(Alert.AlertType.INFORMATION, "Brak zgłoszonych ogłoszeń.");
                        return;
                    }

                    // Tworzenie dialogu
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("Zgłoszone Zlecenia");
                    dialog.setHeaderText("🚩 Ogłoszenia z co najmniej jednym zgłoszeniem");

                    try {
                        dialog.getDialogPane().getStylesheets()
                                .add(getClass().getResource("/style.css").toExternalForm());
                        dialog.getDialogPane().getStyleClass().add("witcher-dialog");
                    } catch (Exception e) {
                    }

                    dialog.getDialogPane().setPrefWidth(600);
                    dialog.getDialogPane().setPrefHeight(500);
                    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

                    VBox listaBox = new VBox(10);
                    listaBox.setPadding(new Insets(10));

                    for (OgloszenieDTO ogl : zgloszone) {
                        HBox wiersz = new HBox(10);
                        wiersz.setAlignment(Pos.CENTER_LEFT);
                        wiersz.setStyle(
                                "-fx-padding: 10; -fx-background-color: rgba(100, 50, 50, 0.3); -fx-background-radius: 8;");

                        VBox info = new VBox(3);
                        info.setAlignment(Pos.CENTER_LEFT);
                        HBox.setHgrow(info, Priority.ALWAYS);

                        Label tytulLbl = new Label(ogl.getTytul());
                        tytulLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                        Label zgloszeniaLbl = new Label(
                                "🚩 Zgłoszenia: " + ogl.getZgloszenia() + " | Autor: " + ogl.getAutor());
                        zgloszeniaLbl.setStyle("-fx-font-size: 11px;");

                        info.getChildren().addAll(tytulLbl, zgloszeniaLbl);

                        Button btnUsun = new Button("🗑 Usuń");
                        btnUsun.getStyleClass().add("delete-button");
                        btnUsun.setOnAction(e -> {
                            // Potwierdzenie usunięcia
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                            confirm.setTitle("Potwierdzenie");
                            confirm.setHeaderText("Usunąć zgłoszone ogłoszenie?");
                            confirm.setContentText("'" + ogl.getTytul() + "' zostanie trwale usunięte.");

                            confirm.showAndWait().ifPresent(btn -> {
                                if (btn == ButtonType.OK) {
                                    new Thread(() -> {
                                        Odpowiedz delOdp = KlientSieciowy.pobierzInstancje()
                                                .wyslijISprawdz(new Zadanie(TypZadania.USUN_OGLOSZENIE, ogl.getId()));
                                        Platform.runLater(() -> {
                                            if (delOdp != null && delOdp.getStatus() == StatusOdpowiedzi.OK) {
                                                utworzAlert(Alert.AlertType.INFORMATION, "Usunięto.");
                                                dialog.close();
                                                odswiezListe();
                                                // Otwórz ponownie dialog zgłoszonych
                                                pokazZgloszone();
                                            } else {
                                                utworzAlert(Alert.AlertType.ERROR, "Błąd usuwania.");
                                            }
                                        });
                                    }).start();
                                }
                            });
                        });

                        wiersz.getChildren().addAll(info, btnUsun);
                        listaBox.getChildren().add(wiersz);
                    }

                    ScrollPane scroll = new ScrollPane(listaBox);
                    scroll.setFitToWidth(true);
                    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

                    dialog.getDialogPane().setContent(scroll);
                    dialog.showAndWait();

                } else {
                    utworzAlert(Alert.AlertType.ERROR, "Błąd pobierania zgłoszonych: " +
                            (odp != null ? odp.getWiadomosc() : "brak odpowiedzi"));
                }
            });
        }).start();
    }

    /**
     * Zgłasza ogłoszenie jako nieodpowiednie
     */
    private void zglosOgloszenie(OgloszenieDTO item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Zgłoszenie");
        confirm.setHeaderText("Czy na pewno chcesz zgłosić '" + item.getTytul() + "' jako nieodpowiednie?");
        confirm.setContentText("Zgłoszenie zostanie przekazane do administratora.");

        try {
            confirm.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            confirm.getDialogPane().getStyleClass().add("witcher-dialog");
        } catch (Exception e) {
        }

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    Zadanie zadanie = new Zadanie(TypZadania.ZGLOS_OGLOSZENIE, item.getId());
                    Odpowiedz odp = KlientSieciowy.pobierzInstancje().wyslijISprawdz(zadanie);
                    Platform.runLater(() -> {
                        if (odp != null && odp.getStatus() == StatusOdpowiedzi.OK) {
                            utworzAlert(Alert.AlertType.INFORMATION, "Ogłoszenie zostało zgłoszone. Dziękujemy!");
                        } else {
                            utworzAlert(Alert.AlertType.ERROR, "Błąd zgłaszania: " +
                                    (odp != null ? odp.getWiadomosc() : "brak odpowiedzi"));
                        }
                    });
                }).start();
            }
        });
    }

    /**
     * Wyświetla szczegóły ogłoszenia w dialogu (zwiększa licznik wyświetleń)
     */
    private void pokazSzczegoly(OgloszenieDTO item) {
        // Zwiększ licznik wyświetleń na serwerze
        new Thread(() -> {
            Zadanie zadanie = new Zadanie(TypZadania.POBIERZ_SZCZEGOLY, item.getId());
            KlientSieciowy.pobierzInstancje().wyslijISprawdz(zadanie);
        }).start();

        // Pokaż dialog ze szczegółami
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Szczegóły ogłoszenia");
        dialog.setHeaderText(item.getTytul());

        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("witcher-dialog");
        } catch (Exception e) {
        }

        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("ad-card");

        // Kategoria
        HBox kategoriaBox = new HBox(10);
        Label lblKat = new Label("Kategoria:");
        lblKat.setStyle("-fx-font-weight: bold;");
        Label valKat = new Label(item.getKategoria());
        kategoriaBox.getChildren().addAll(lblKat, valKat);

        // Autor
        HBox autorBox = new HBox(10);
        Label lblAutor = new Label("Autor:");
        lblAutor.setStyle("-fx-font-weight: bold;");
        Label valAutor = new Label(item.getAutor());
        autorBox.getChildren().addAll(lblAutor, valAutor);

        // Data
        HBox dataBox = new HBox(10);
        Label lblData = new Label("Data:");
        lblData.setStyle("-fx-font-weight: bold;");
        String dataStr = item.getDataDodania() != null
                ? item.getDataDodania().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                : "Nieznana";
        Label valData = new Label(dataStr);
        dataBox.getChildren().addAll(lblData, valData);

        // Statystyki
        HBox statsBox = new HBox(20);
        Label viewsLbl = new Label("👁 Wyświetlenia: " + item.getWyswietlenia());
        Label reportsLbl = new Label("🚩 Zgłoszenia: " + item.getZgloszenia());
        statsBox.getChildren().addAll(viewsLbl, reportsLbl);

        // Treść
        VBox trescBox = new VBox(5);
        Label lblTresc = new Label("Treść:");
        lblTresc.setStyle("-fx-font-weight: bold;");
        TextArea trescArea = new TextArea(item.getTresc());
        trescArea.setWrapText(true);
        trescArea.setEditable(false);
        trescArea.setPrefRowCount(5);
        trescBox.getChildren().addAll(lblTresc, trescArea);

        // Kontakt
        if (item.getDaneKontaktowe() != null && !item.getDaneKontaktowe().isEmpty()) {
            HBox kontaktBox = new HBox(10);
            Label lblKontakt = new Label("📞 Kontakt:");
            lblKontakt.setStyle("-fx-font-weight: bold;");
            Label valKontakt = new Label(item.getDaneKontaktowe());
            kontaktBox.getChildren().addAll(lblKontakt, valKontakt);
            content.getChildren().add(kontaktBox);
        }

        content.getChildren().addAll(kategoriaBox, autorBox, dataBox, statsBox, trescBox);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
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
