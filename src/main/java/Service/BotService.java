package Service;

import Model.Currency;
import Model.User;
import com.itextpdf.text.DocumentException;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

public interface BotService {
    SendMessage start(String chatId);

    SendMessage deleteKeyboard(String chatId);

    SendMessage shareContact(String chatId);

    SendMessage adminUser(String chatId);

    SendDocument userList(TreeMap<String, User> userMap, String chatId) throws IOException;

    SendDocument currencyList(String chatId) throws IOException, DocumentException;

    int getVerificationCode(String phoneNumber);

    List<Currency> getCurrencies() throws IOException;

    EditMessageText currenciesCard(CallbackQuery callbackQuery, Chat chat, String data, int first, int last) throws IOException;

    InlineKeyboardMarkup currenciesButtons(List<Currency> currencies, String data, int first, int last);

    boolean isNumeric(String string);

    void conversionList(Currency from, Currency to, String result, String userName, String input) throws IOException;

    SendPhoto advert(String chatId) throws IOException;

    SendMessage news(String chatId) throws IOException;
}
