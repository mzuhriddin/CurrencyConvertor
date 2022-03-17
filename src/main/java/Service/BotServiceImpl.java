package Service;

import Model.Currency;
import Model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.twilio.Twilio.init;
import static java.math.BigInteger.valueOf;

public class BotServiceImpl implements BotService {
    @Override
    public SendMessage start(String chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setRequestContact(true);
        button.setText("Share Contact");

        row.add(button);
        rows.add(row);
        replyKeyboardMarkup.setKeyboard(rows);

        return SendMessage.builder()
                .text("Xush kelibsz!")
                .chatId(chatId)
                .replyMarkup(replyKeyboardMarkup)
                .build();
    }

    @Override
    public SendMessage deleteKeyboard(String chatId) {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        return SendMessage.builder()
                .text("Royxatdan ottingiz")
                .chatId(chatId)
                .replyMarkup(replyKeyboardRemove)
                .build();
    }

    @Override
    public SendMessage shareContact(String chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button1 = new KeyboardButton();
        KeyboardButton button2 = new KeyboardButton();
        button1.setText("Admin User");
        button2.setText("Simple User");
        row.add(button1);
        row.add(button2);
        rows.add(row);
        replyKeyboardMarkup.setKeyboard(rows);
        return SendMessage.builder()
                .text("User turini tanlang")
                .chatId(chatId)
                .replyMarkup(replyKeyboardMarkup)
                .build();
    }

    @Override
    public SendMessage adminUser(String chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        InlineKeyboardButton button5 = new InlineKeyboardButton();

        button1.setCallbackData("userList&");
        button2.setCallbackData("allConversions&");
        button3.setCallbackData("currencies&");
        button4.setCallbackData("news&");
        button5.setCallbackData("advert&");

        button1.setText("Users list");
        button2.setText("All Conversions");
        button3.setText("Currency list");
        button4.setText("News");
        button5.setText("Advertisement");

        row1.add(button1);
        row1.add(button2);
        row2.add(button3);
        row2.add(button4);
        row3.add(button5);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        inlineKeyboardMarkup.setKeyboard(rows);

        return SendMessage.builder()
                .text("Admin paneliga xush kelisbsiz!")
                .chatId(chatId)
                .replyMarkup(inlineKeyboardMarkup)
                .build();

    }

    @Override
    public SendDocument userList(TreeMap<String, User> userMap, String chatId) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/zuhriddin/Downloads/CurrencyConvertor/src/main/resources/UserList.xlsx");
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Users");
        int rowNum = 0;
        HSSFRow rowHead = sheet.createRow(rowNum++);
        rowHead.createCell(0).setCellValue("Firstname");
        rowHead.createCell(1).setCellValue("Lastname");
        rowHead.createCell(2).setCellValue("Id");
        rowHead.createCell(3).setCellValue("Username");
        rowHead.createCell(4).setCellValue("IsBot");
        rowHead.createCell(5).setCellValue("Contact");

        for (Map.Entry<String, User> entry : userMap.entrySet()) {
            User v = entry.getValue();
            HSSFRow row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(v.getFirstName());
            row.createCell(1).setCellValue(v.getLastName());
            row.createCell(2).setCellValue(v.getId());
            row.createCell(3).setCellValue(v.getUserName());
            row.createCell(4).setCellValue(v.isBot());
            row.createCell(5).setCellValue(v.getContact());
            rowNum++;
        }
        for (int i = 0; i < rowHead.getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(fileOutputStream);
        fileOutputStream.close();
        workbook.close();

        return SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(new File("/Users/zuhriddin/Downloads/CurrencyConvertor/src/main/resources/UserList.xlsx")))
                .build();
    }

    @Override
    public SendDocument currencyList(String chatId) throws IOException, DocumentException {
        HttpGet httpGet = new HttpGet("https://cbu.uz/oz/arkhiv-kursov-valyut/json/");
        HttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(httpGet);
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        List<Currency> currencies = gson.fromJson(reader, new TypeToken<List<Currency>>() {
        }.getType());

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("/Users/zuhriddin/Downloads/CurrencyConvertor/src/main/resources/currencyList.pdf"));

        document.open();

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        float[] columnWidths = {1f, 1f, 1f, 1f};
        table.setWidths(columnWidths);
        table.addCell("Nomi");
        table.addCell("Name");
        table.addCell("Abbreviation");
        table.addCell("Rate");
        table.setHeaderRows(1);
        PdfPCell[] cells = table.getRow(0).getCells();
        for (PdfPCell cell : cells) {
            cell.setBackgroundColor(BaseColor.GRAY);
        }

        for (Currency currency : currencies) {
            table.addCell(currency.getCcyNm_UZ());
            table.addCell(currency.getCcyNm_EN());
            table.addCell(currency.getCcy());
            table.addCell(currency.getRate());
        }
        document.add(table);
        document.close();

        return SendDocument.builder()
                .chatId(chatId)
                .document(new InputFile(new File("/Users/zuhriddin/Downloads/CurrencyConvertor/src/main/resources/currencyList.pdf")))
                .build();
    }

    @Override
    public int getVerificationCode(String phoneNumber) {
        String ACCOUNT_SID = "ACCOUNT_SID";
        String AUTH_TOKEN = "AUTH_TOKEN";
        init(ACCOUNT_SID, AUTH_TOKEN);

        int randomCode = (int) (Math.random() * 89999 + 10000);
        Message.creator(new PhoneNumber(phoneNumber), // to
                new PhoneNumber("+123456789"), // from
                String.valueOf(randomCode)).create();
        return randomCode;
    }

    @Override
    public List<Currency> getCurrencies() throws IOException {
        HttpGet get = new HttpGet("https://cbu.uz/uz/arkhiv-kursov-valyut/json/");
        HttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(get);
        Gson gson = new Gson();
        List<Currency> currencies = gson.fromJson(
                EntityUtils.toString(response.getEntity()),
                new TypeToken<List<Currency>>() {
                }.getType());
        Currency currency = new Currency();
        currency.setCcy("UZS");
        currency.setId(0);
        currency.setCcyNm_UZ("O'zbek so'mi");
        currency.setCcyNm_UZC("Ўзбек сўми");
        currency.setCcyNm_EN("Uzbek sum");
        currency.setCcyNm_RU("Узбекский сум");
        currencies.add(0, currency);
        currency.setRate("1.0");
        return currencies;
    }

    public List<Currency> getCurrencies(int id) throws IOException {
        List<Currency> currencies = new ArrayList<>();
        for (Currency currency : getCurrencies()) {
            if (currency.getId() != id) {
                currencies.add(currency);
            }
        }
        return currencies;
    }

    @Override
    public EditMessageText currenciesCard(CallbackQuery callbackQuery, Chat chat, String data, int first, int last) throws IOException {
        EditMessageText sendMessage = new EditMessageText();
        sendMessage.setChatId(chat.getId().toString());
        sendMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        List<Currency> currencies;
        String[] parsed_data = data.split("&");
        if (parsed_data.length > 1) {
            currencies = getCurrencies(Integer.parseInt(parsed_data[1]));
        } else {
            currencies = getCurrencies();
        }
        sendMessage.setText(parsed_data.length > 1 ? "Qanday valyutaga konvertatsiya qilinsin:" : "Konvertatsiya valyutasi: ");
        InlineKeyboardMarkup markup = currenciesButtons(currencies, data, first, last);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    @Override
    public InlineKeyboardMarkup currenciesButtons(List<Currency> currencies, String data, int first, int last) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (int i = first; i < last; i += 3) {
            List<InlineKeyboardButton> buttonRow = new ArrayList<>();
            for (int i1 = 0; i1 < 3; i1++) {
                InlineKeyboardButton btn = new InlineKeyboardButton();
                if (currencies.size() <= i + i1) {
                    break;
                }
                Currency currency = currencies.get(i + i1);
                btn.setText(currency.getCcyNm_UZ());
                btn.setCallbackData(data + "&" + currency.getId());
                buttonRow.add(btn);
            }
            buttons.add(buttonRow);
        }
        List<InlineKeyboardButton> btnRow = new ArrayList<>();
        InlineKeyboardButton btnLast = new InlineKeyboardButton("⬅️");
        InlineKeyboardButton btnNext = new InlineKeyboardButton("➡️");
        String[] parsed_data = data.split("&");
        btnLast.setCallbackData("last&" + first + (parsed_data.length > 1 ? "&" + parsed_data[1] : ""));
        btnNext.setCallbackData("next&" + (last - 1) + (parsed_data.length > 1 ? "&" + parsed_data[1] : ""));
        btnRow.add(btnLast);
        btnRow.add(btnNext);
        buttons.add(btnRow);
        markup.setKeyboard(buttons);
        return markup;
    }

    @Override
    public boolean isNumeric(String string) {

        System.out.printf("Parsing string: \"%s\"%n", string);

        if (string == null || string.equals("")) {
            System.out.println("String cannot be parsed, it is null or empty.");
            return false;
        }

        try {
            valueOf(Long.parseLong(string));
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Input String cannot be parsed to Integer.");
        }
        return false;
    }

    @Override
    public void conversionList(Currency from, Currency to, String result, String userName, String input) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("/Users/zuhriddin/Downloads/CurrencyConvertor/src/main/resources/ConversionList.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
        XSSFSheet sheet = workbook.getSheet("Conversion");
//        CellStyle tCs = workbook.createCellStyle();
//        tCs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        tCs.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
//
//        Row rowHead = sheet.createRow(0);
//        rowHead.createCell(0).setCellValue("Username");
//        rowHead.createCell(1).setCellValue("From");
//        rowHead.createCell(2).setCellValue("Input");
//        rowHead.createCell(3).setCellValue("To");
//        rowHead.createCell(4).setCellValue("Result");
//
//        for (int k = 0; k < 5; k++) {
//            sheet.getRow(0).getCell(k).setCellStyle(tCs);
//        }
        int rowNum = sheet.getLastRowNum();

        XSSFRow row = sheet.createRow(++rowNum);
        row.createCell(0).setCellValue(userName);
        row.createCell(1).setCellValue(from.getCcyNm_UZ());
        row.createCell(2).setCellValue(input);
        row.createCell(3).setCellValue(to.getCcyNm_UZ());
        row.createCell(4).setCellValue(result);

        for (int i = 0; i < row.getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream fileOutputStream = new FileOutputStream("/Users/zuhriddin/Downloads/CurrencyConvertor/src/main/resources/ConversionList.xlsx");
        fileInputStream.close();
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        workbook.close();
    }

    @Override
    public SendPhoto advert(String chatId) throws IOException {
//        HttpGet httpGet = new HttpGet("https://avatars.mds.yandex.net/get-adfox-content/2367573/220115_adfox_1749280_4888624.dc229335fec4407f04de3d25bb9e0c59.jpg/optimize.webp");
//        CloseableHttpClient client = HttpClientBuilder.create().build();
//        HttpResponse response = client.execute(httpGet);
//        HttpEntity entity = response.getEntity();
//        InputStream inputStream = entity.getContent();
//
//        FileOutputStream fileOutputStream = new FileOutputStream("/Users/zuhriddin/Downloads/CurrencyConvertor/src/main/resources/advert.jpg");
//        int inByte;
//        while ((inByte = inputStream.read()) != -1){
//            fileOutputStream.write(inByte);
//        }
//        inputStream.close();
//        fileOutputStream.close();
//        client.close();
        return SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(new File("/Users/zuhriddin/Downloads/CurrencyConvertor/src/main/resources/advert.jpg")))
                .caption("<a href=\"https://anorbank.uz\">Havolaga bosing</a>").parseMode(ParseMode.HTML)
                .build();
    }

    @Override
    public SendMessage news(String chatId) throws IOException {
        Connection connect = Jsoup.connect("https://www.bbc.com/news/world-asia-60257080");
        org.jsoup.nodes.Document document = connect.get();
        Elements dateList1 = document.getElementsByClass("ssrcss-uf6wea-RichTextComponentWrapper e1xue1i85");
        StringBuilder builder = new StringBuilder();
        for (Element element : dateList1) {
            Elements p = element.getElementsByTag("p");
            for (Element element1 : p) {
                builder.append(element1.ownText()).append("\n\n");
            }
        }
        return SendMessage.builder()
                .chatId(chatId)
                .text(builder.toString())
                .build();
    }
}
