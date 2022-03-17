import Model.Currency;
import Model.User;
import Service.BotServiceImpl;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class Bot extends TelegramLongPollingBot {
    public static int verificationCode = 0;
    TreeMap<String, User> userMap = new TreeMap<>();
    TreeMap<String, String> chatIds = new TreeMap<>();
    public static Currency currencyFrom;
    public static Currency currencyTo;
    public String state = null;
    BotServiceImpl botService = new BotServiceImpl();

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new Bot());

    }

    @Override
    public String getBotUsername() {
        return "Zuck";
    }

    @Override
    public String getBotToken() {
        return "5018652777:AAHYJD6Z8nJUBY4HXG87T10hVAY30oATDiY";
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updates.forEach(update -> new Thread(() -> this.onUpdateReceived(update)).start());
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        User user = new User();
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String text = message.getText();
            String chatId = String.valueOf(message.getChatId());


            if (message.hasText()) {
                if (text.equals("/start")) {
                    execute(botService.start(chatId));
                    state = BotState.START;
                    chatIds.put(message.getFrom().getUserName(), chatId);
                } else if (botService.isNumeric(text)) {
//                    if (String.valueOf(verificationCode).equals(text)) {
//                        execute(botService.shareContact(chatId));
//                        state = BotState.VERIFICATION;
//                    }
                    Float result = (Float.parseFloat(text) * Float.parseFloat(currencyFrom.getRate()) / Float.parseFloat(currencyTo.getRate()));
                    execute(SendMessage.builder().chatId(chatId).text(String.format("%.2f", result)).build());
                    botService.conversionList(currencyFrom, currencyTo, String.format("%.2f", result), message.getFrom().getUserName(), text);

                } else if (text.startsWith("+") && botService.isNumeric(text.substring(1))) {
                    execute(botService.deleteKeyboard(chatId));
                    execute(botService.shareContact(chatId));
//                    verificationCode = botService.getVerificationCode(text);
                    user.setId(String.valueOf(message.getFrom().getId()));
                    user.setBot(message.getFrom().getIsBot());
                    user.setLastName(message.getFrom().getLastName());
                    user.setFirstName(message.getFrom().getFirstName());
                    user.setUserName(message.getFrom().getUserName());
                    user.setContact(text);
                    userMap.put(user.getUserName(), user);
                    state = BotState.CONTACT;
                } else if (text.equals("Admin User")) {
                    execute(SendMessage.builder().chatId(chatId).text("Admin parolini kiriting!").build());
                } else if (text.equals("admin")) {
                    execute(botService.adminUser(chatId));
                    state = BotState.ADMIN;
                } else if (text.equals("Simple User")) {
                    List<Currency> currencies = botService.getCurrencies();
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Konvertatsiya valyutasi:");
                    InlineKeyboardMarkup markup = botService.currenciesButtons(currencies, "convert", 0, 9);
                    sendMessage.setReplyMarkup(markup);
                    execute(sendMessage);
                } else execute(SendMessage.builder().chatId(chatId).text("Xatolik! Qaytadan urinib koring!").build());
            } else if (message.hasContact()) {
                execute(botService.deleteKeyboard(chatId));
                execute(botService.shareContact(chatId));
//                verificationCode = botService.getVerificationCode("+" + message.getContact().getPhoneNumber());
                user.setId(String.valueOf(message.getFrom().getId()));
                user.setBot(message.getFrom().getIsBot());
                user.setLastName(message.getFrom().getLastName());
                user.setFirstName(message.getFrom().getFirstName());
                user.setUserName(message.getFrom().getUserName());
                user.setContact("+" + message.getContact().getPhoneNumber());
                userMap.put(user.getUserName(), user);
            }else execute(SendMessage.builder().chatId(chatId).text("Xatolik! Qaytadan urinib koring!").build());

        } else if (update.hasCallbackQuery()) {
            String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
            String[] query = update.getCallbackQuery().getData().split("&");
            Chat chat = update.getCallbackQuery().getMessage().getChat();
            if (query.length > 0) {
                switch (query[0]) {
                    case "userList" -> execute(botService.userList(userMap, chatId));
                    case "currencies" -> execute(botService.currencyList(chatId));
                    case "allConversions" -> execute(SendDocument.builder().chatId(chatId).document(new InputFile(new File("/Users/zuhriddin/Downloads/CurrencyConvertor/src/main/resources/ConversionList.xlsx"))).build());
                    case "last" -> {
                        if (Integer.parseInt(query[1]) == 0) {
                            AnswerCallbackQuery answer = new AnswerCallbackQuery(update.getCallbackQuery().getId());
                            answer.setText("Bu ro'yxatning boshlanishi");
                            execute(answer);
                        } else {
                            int first_index = Integer.parseInt(query[1]);
                            EditMessageText sendMessage = botService.currenciesCard(update.getCallbackQuery(), chat, "convert" + (query.length > 2 ? '&' + query[2] : ""), first_index - 9, first_index);
                            execute(sendMessage);
                        }
                    }
                    case "next" -> {
                        List<Currency> currencies = botService.getCurrencies();
                        if (Integer.parseInt(query[1]) >= currencies.size() - 1) {
                            AnswerCallbackQuery answer = new AnswerCallbackQuery(update.getCallbackQuery().getId());
                            answer.setText("Bu ro'yxatning oxiri");
                            execute(answer);
                        } else {
                            int first_index = Integer.parseInt(query[1]);
                            EditMessageText sendMessage = botService.currenciesCard(update.getCallbackQuery(), chat, "convert" + (query.length > 2 ? '&' + query[2] : ""), first_index + 1, first_index + 10);
                            execute(sendMessage);
                        }
                    }
                    case "convert" -> {
                        if (query.length == 2) {
                            EditMessageText sendMessage = botService.currenciesCard(update.getCallbackQuery(), chat, "convert&" + query[1], 0, 9);
                            execute(sendMessage);
                        } else if (query.length == 3) {
                            EditMessageText editMessage = new EditMessageText();
                            editMessage.setChatId(chat.getId().toString());
                            editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                            List<Currency> currencies = botService.getCurrencies();
                            currencies.sort(Comparator.comparing(Currency::getId));
                            currencyFrom = currencies.get(Integer.parseInt(query[1]));
                            currencyTo = currencies.get(Integer.parseInt(query[2]));
                            editMessage.setText(currencyFrom.getCcyNm_UZ() + "dan\n" + currencyTo.getCcyNm_UZ() + "ga\nconvertatsiya uchun kerakli summani jo'nating");
                            execute(editMessage);
                        }
                    }
                    case "advert" -> chatIds.forEach((k, v) -> {
                        try {
                            execute(botService.advert(v));
                        } catch (TelegramApiException | IOException e) {
                            e.printStackTrace();
                        }
                    });
                    case "news" -> chatIds.forEach((k, v) -> {
                        try {
                            execute(botService.news(v));
                        } catch (TelegramApiException | IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }
}



