package com.yandex.ajwar.view;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.yandex.ajwar.MainApp;
import com.yandex.ajwar.model.DocTypes;
import com.yandex.ajwar.model.Formats;
import com.yandex.ajwar.model.InputMask;
import com.yandex.ajwar.model.StringData;
import com.yandex.ajwar.util.AlertUtilNew;
import com.yandex.ajwar.util.S4AppUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by 53189 on 22.11.2016.
 */
public class OptionsController implements Initializable{

    private MainApp mainApp;
    private S4AppUtil S4App= S4AppUtil.getInstance();
    private static final Logger log=Logger.getLogger(MainApp.class);
    private Stage optionsDialogStage;
    private Preferences preferencesScanKdAndTd=MainApp.getPreferencesScanKdAndTd();
    private Preferences preferencesTableViewDocTypes=MainApp.getPreferencesTableViewDocTypes();
    private Preferences preferencesTableViewInputMask =MainApp.getPreferencesTableViewInputMask();
    private Preferences preferencesTableViewFormats =MainApp.getPreferencesTableViewFormats();
    private static final boolean FL=false;
    private final String[] listMask={
                                    "ВИЕЛ.**.*.***.***",
                                    "ВИЕЛ.******.***",
                                    "*ВЩ.***.***",
                                    "Э-*.**.*.***.***",
                                    "ЭСКИЗ-*.**.*.***.***",
                                    "**-****",
                                    "**-**** Дополнение №*"};
    private ExecutorService executorServiceLoad;
    //private static ExecutorService executorServiceLoad=getMainApp().getPdfViewerController().getExecutorServiceLoad();
    //private PdfViewerController pdfViewerController=getMainApp().getPdfViewerController();

    //private ObservableList<DocTypes> docTypesObservableList= FXCollections.observableArrayList();
    @FXML
    private TableView<InputMask> tableViewInputMask;
    @FXML
    private TableColumn<InputMask,String> tableColumnMask;
    @FXML
    private TableColumn<InputMask,Number> tableColNumberInputMask;
    @FXML
    private TableView<DocTypes> tableViewDocTypes;
    @FXML
    private TableColumn<DocTypes,String> tableColumnDocName;
    @FXML
    private TableColumn<DocTypes,String> tableColumnDtCode;
    @FXML
    private TableColumn<DocTypes,Number> tableColNumberDocTypes;
    @FXML
    private TableView<Formats> tableViewFormats;
    @FXML
    private TableColumn<Formats,String> tableColumnFormats;
    @FXML
    private TableColumn<Formats,Number> tableColNumberFormats;
    @FXML
    private TextField textFieldFormats;
    @FXML
    private TextField textFieldFolderScan;
    @FXML
    private TextField textFieldFolderMove;
    @FXML
    private ComboBox<String> comboBoxScanKDTypeDoc;
    @FXML
    private Label labelSaveNotice;
    @FXML
    private TextField textFieldArchiveId;
    @FXML
    private ComboBox<String> comboBoxMask;
    @FXML
    private CheckBox checkBoxScanKdOptions;
    @FXML
    private CheckBox checkBoxScanTdOptions;
    @FXML
    private CheckBox checkBoxScanKdNewOptions;
    @FXML
    private CheckBox checkBoxScanKdVersionOptions;

    /**вешаю слушателя на чекбоксы скрытия Tab'ov*/
    private void addListenerCheckBox(){
        checkBoxScanKdOptions.selectedProperty().addListener((observable, oldValue, newValue) -> {
                checkBoxScanKdNewOptions.setDisable(!newValue);
                checkBoxScanKdVersionOptions.setDisable(!newValue);
                checkBoxScanKdNewOptions.selectedProperty().set(newValue);
                checkBoxScanKdVersionOptions.selectedProperty().set(newValue);
        });
    }

    /**Обновление формы PdfView при нажатии на кнопку сохранить*/
    public void updatePdfViewAfterClickSave(){
        //Cначала идет заполнение всех комбоБоксов на форме
        getMainApp().getPdfViewerController().containComboBoxDocTypes();
        getMainApp().getPdfViewerController().containComboBoxFormats();
        getMainApp().getPdfViewerController().containComboBoxMask();
        getMainApp().getPdfViewerController().disableFormPdfView();
    }
    /**Инициализация кэшированного пула потоков*/
    private void createAndConfigureExecutorsLoadService() {
        executorServiceLoad = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
    }
    /**Первичная и единственная инициализация таблицы Форматов*/
    public void containTableViewFormats(){
        if (!preferencesScanKdAndTd.getBoolean("FL",false)){
            String[] format={"А1","А2","А2х3","А2х4","А3","А3х3","А3х4","А4","А4х3","А4х4"};
            for (int i = 0; i <format.length ; i++) {
                tableViewFormats.getItems().add(new Formats(format[i]));
            }
            preferencesScanKdAndTd.putBoolean("FL",true);
        }
    }
    /**Закрытие диалогового окна*/
    public void onClickMouseButtonCancel(){
        optionsDialogStage.close();
    }
    /**Сохранение в реестр при нажатии на кнопку "Готово" и закрытие диалогового окна*/
    public void onClickMouseButtonReady(){
        onClickMouseButtonSave();
        optionsDialogStage.close();
    }
    /**Сохранение в реестр при нажатии на кнопку "Сохранить"*/
    public void onClickMouseButtonSave(){
        if (textFieldArchiveId.getText().isEmpty()) {
            AlertUtilNew.message("Внимание!","Выберите архив из списка архивов Search!","Пустое поле архивы.", Alert.AlertType.WARNING);
        }else {
            preferencesScanKdAndTd.putLong("textFieldArchiveId", Long.parseLong(textFieldArchiveId.getText()));
            preferencesScanKdAndTd.put("textFieldFolderScan", textFieldFolderScan.getText());
            preferencesScanKdAndTd.put("textFieldFolderMove", textFieldFolderMove.getText());
            preferencesScanKdAndTd.putBoolean("checkBoxScanKdOptions",checkBoxScanKdOptions.selectedProperty().get());
            preferencesScanKdAndTd.putBoolean("checkBoxScanKdNewOptions",checkBoxScanKdNewOptions.selectedProperty().get());
            preferencesScanKdAndTd.putBoolean("checkBoxScanKdVersionOptions",checkBoxScanKdVersionOptions.selectedProperty().get());
            preferencesScanKdAndTd.putBoolean("checkBoxScanTdOptions",checkBoxScanTdOptions.selectedProperty().get());
            checkBoxScanTdOptions.selectedProperty().set(preferencesScanKdAndTd.getBoolean("checkBoxScanTdOptions",true));
            try {
                preferencesTableViewDocTypes.clear();
                preferencesTableViewInputMask.clear();
                preferencesTableViewFormats.clear();
            } catch (BackingStoreException e) {
                log.error("Ошибка при очистке реестра.",e);
                e.printStackTrace();
            }
            for (DocTypes types : tableViewDocTypes.getItems()) {
                preferencesTableViewDocTypes.put(types.getDocName(), types.getDtCode());
            }
            int i = 0;
            for (InputMask inputMask : tableViewInputMask.getItems()) {
                preferencesTableViewInputMask.put(inputMask.getMask(), "" + i++);
            }
            i = 0;
            for (Formats formats : tableViewFormats.getItems()) {
                preferencesTableViewFormats.put(formats.getFormat(), "" + i++);
            }
            labelSaveNotice.setText("Изменения сохранены.");
            Platform.runLater(() ->updatePdfViewAfterClickSave());
        }
    }
    /**Заполнения таблицы Маски по нажатию кнопки "Добавить"*/
    public void addInputMask(){
        final String text= comboBoxMask.getSelectionModel().getSelectedItem();
        if (!text.isEmpty() && !checkTableView(text,tableViewInputMask)) tableViewInputMask.getItems().add(new InputMask(text));
    }
    /**Выбор архива из Search*/
    public void addArchiveButton(){
        textFieldArchiveId.setText(String.valueOf(S4App.chooseArchiveName(S4App)));
    }
    /**Удаление из таблицы Маски строки при нажатии на кнопку "Удалить маску"*/
    public void deleteMaskButton(){
        if (tableViewInputMask.getSelectionModel().getSelectedItem()!=null) {
            tableViewInputMask.getItems().remove(tableViewInputMask.getSelectionModel().getSelectedItem());
            tableViewInputMask.refresh();
        }
    }
    /**Заполнение выпадающего списка,где можно выбирать типы документов*/
    public void containComboBoxScanKDTypeDoc(){
        //открываю новый поток Jacob и создаю новый объект Серча
        S4AppUtil S4AppThread=S4AppUtil.returnAndCreateThreadS4App();
        try {
            //Работаю уже с новым объектом серча в новом потоке и ищу только типы документов с расширением PDF
            S4AppThread.openQuery(S4AppThread,"Select doc_name from doctypes where doc_ext=\"pdf\" order by doc_name");
            for (S4AppThread.queryGoFirst(S4AppThread); S4AppThread.queryEOF(S4AppThread)==0 ; S4AppThread.queryGoNext(S4AppThread)) {
                comboBoxScanKDTypeDoc.getItems().add(S4AppThread.queryFieldByName(S4AppThread,"doc_name"));
            }
            S4AppThread.closeQuery(S4AppThread);
            comboBoxScanKDTypeDoc.getSelectionModel().select(0);
        } finally {
            //Закрываю поток в любом случае
            S4AppUtil.closeThreadS4App();
        }

    }
    /**
     * Заполнение таблицы Типы документов по нажатию кнопки "Добавить"
     */
    public void addDocTypeButton(){
        final String temp=comboBoxScanKDTypeDoc.getSelectionModel().getSelectedItem();
        if (!checkTableView(temp,tableViewDocTypes)) {
            S4App.openQuery(S4App,"select dt_code from doctypes where doc_name=\"" + temp + "\"");
            tableViewDocTypes.getItems().add(new DocTypes(temp, S4App.queryFieldByName(S4App,"dt_code")));
            S4App.closeQuery(S4App);
        }
    }
    /**Заполнение таблицы Форматы по нажатию кнопки "Добавить"*/
    public void addFormatsButton(){
        final String temp=textFieldFormats.getText();
        if (!checkTableView(temp,tableViewFormats) && !temp.equals("")) {
            tableViewFormats.getItems().add(new Formats(temp));
        }
    }

    /**Проверяю на дубли таблицы масок,наименований и типов документов и форматов*/
    private boolean checkTableView(String str,TableView table){
        boolean flag=false;
        for (Object object :table.getItems()) {
            if (object instanceof DocTypes){
                flag=((DocTypes) object).getDocName().equalsIgnoreCase(str)?true:false;
            }
            if (object instanceof InputMask){
                flag=((InputMask) object).getMask().equalsIgnoreCase(str)?true:false;
            }
            if (object instanceof  Formats){
                flag=((Formats) object).getFormat().equalsIgnoreCase(str)?true:false;
            }
        }
        return flag;
    }
    /**Удаление из таблицы типов документов строки при нажатии на кнопку "Удалить строку документов"*/
    public void deleteDocTypeButton(){
        if (tableViewDocTypes.getSelectionModel().getSelectedItem()!=null) {
            tableViewDocTypes.getItems().remove(tableViewDocTypes.getSelectionModel().getSelectedItem());
            tableViewDocTypes.refresh();
        }
    }
    /**Удаление из таблицы типов документов строки при нажатии на кнопку "Удалить строку форматов"*/
    public void deleteFormatsButton(){
        if (tableViewFormats.getSelectionModel().getSelectedItem()!=null) {
            tableViewFormats.getItems().remove(tableViewFormats.getSelectionModel().getSelectedItem());
            tableViewFormats.refresh();
        }
    }

    /**Устанавливаю тип и значение,которое должно хранится в колонке,а также заполняю comboBoxMask масками*/
    private void initTableColumns(){
        tableColNumberDocTypes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(tableViewDocTypes.getItems().indexOf(param.getValue())+1));
        tableColumnDocName.setCellValueFactory(new PropertyValueFactory<>("docName"));
        tableColumnDtCode.setCellValueFactory(new PropertyValueFactory<>("dtCode"));
        tableColNumberInputMask.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(tableViewInputMask.getItems().indexOf(param.getValue())+1));
        tableColumnMask.setCellValueFactory(new PropertyValueFactory<>("mask"));
        tableColumnFormats.setCellValueFactory(new PropertyValueFactory<>("format"));
        tableColNumberFormats.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(tableViewFormats.getItems().indexOf(param.getValue())+1));
        comboBoxMask.getItems().addAll(listMask);
        comboBoxMask.getSelectionModel().select(0);
        containTableViewFormats();//Первичная и единственная инициализация таблицы форматов
    }

    /**Чтение данных из реестра,для задания значений в форме Options(TextField and e.g. )*/
    private void loadBaseOptions(){
        getTextFieldArchiveId().setText(String.valueOf(preferencesScanKdAndTd.getLong("textFieldArchiveId",323)));
        getTextFieldFolderMove().setText(preferencesScanKdAndTd.get("textFieldFolderMove",System.getProperty("user.home")));
        getTextFieldFolderScan().setText(preferencesScanKdAndTd.get("textFieldFolderScan", System.getProperty("user.home")));
        checkBoxScanKdOptions.selectedProperty().set(preferencesScanKdAndTd.getBoolean("checkBoxScanKdOptions",true));
        checkBoxScanKdNewOptions.selectedProperty().set(preferencesScanKdAndTd.getBoolean("checkBoxScanKdNewOptions",true));
        checkBoxScanKdVersionOptions.selectedProperty().set(preferencesScanKdAndTd.getBoolean("checkBoxScanKdVersionOptions",true));
        checkBoxScanTdOptions.selectedProperty().set(preferencesScanKdAndTd.getBoolean("checkBoxScanTdOptions",true));
        try {
            String keysDocTypes[]= preferencesTableViewDocTypes.keys();
            String keysInputMask[]=preferencesTableViewInputMask.keys();
            String keysFormats[]=preferencesTableViewFormats.keys();
            for (int i = 0; i <keysDocTypes.length ; i++) {
                tableViewDocTypes.getItems().add(new DocTypes(keysDocTypes[i], preferencesTableViewDocTypes.get(keysDocTypes[i],null)));
            }
            for (int i = 0; i <keysInputMask.length ; i++) {
                tableViewInputMask.getItems().add(new InputMask(keysInputMask[i]));
            }
            for (int i = 0; i <keysFormats.length ; i++) {
                tableViewFormats.getItems().add(new Formats(keysFormats[i]));
            }
        } catch (BackingStoreException e) {
            log.error("Ошибка при чтении реестра и заполнения таблиц.",e);
            e.printStackTrace();
        }
    }

    /**При нажатии на кнопку,выбирается папка сканирования*/
    public void openFolderScanButton(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку,в которую ложатся сканированные файлы.");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = directoryChooser.showDialog(getMainApp().getPrimaryStage());
        if (file == null) textFieldFolderScan.setText(System.getProperty("user.home"));
        else textFieldFolderScan.setText(file.getAbsolutePath());
    }
    /**При нажатии на кнопку,выбирается папка перемещения доков,после сканирования*/
    public void openFolderMoveButton(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку,в которую будут перемещены сканированные файлы.");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = directoryChooser.showDialog(getMainApp().getPrimaryStage());
        if (file == null) textFieldFolderMove.setText(System.getProperty("user.home"));
        else textFieldFolderMove.setText(file.getAbsolutePath());
    }


    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    public  MainApp getMainApp() {
        return mainApp;
    }

    public Stage getOptionsDialogStage() {
        return optionsDialogStage;
    }

    public void setOptionsDialogStage(Stage optionsDialogStage) {
        this.optionsDialogStage = optionsDialogStage;
    }

    public TextField getTextFieldFolderScan() {
        return textFieldFolderScan;
    }

    public TextField getTextFieldFolderMove() {
        return textFieldFolderMove;
    }

    public TableView<DocTypes> getTableViewDocTypes() {
        return tableViewDocTypes;
    }

    public TextField getTextFieldArchiveId() {
        return textFieldArchiveId;
    }

    public void setTextFieldArchiveId(TextField textFieldArchiveId) {
        this.textFieldArchiveId = textFieldArchiveId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createAndConfigureExecutorsLoadService();
        initTableColumns();
        executorServiceLoad.submit(this::containComboBoxScanKDTypeDoc);
        addListenerCheckBox();
        loadBaseOptions();
    }
}
