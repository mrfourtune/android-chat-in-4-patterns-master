package nju.androidchat.client.mvp0;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.java.Log;
import nju.androidchat.client.ClientMessage;
import nju.androidchat.client.R;
import nju.androidchat.client.Utils;
import nju.androidchat.client.component.ItemTextReceive;
import nju.androidchat.client.component.ItemTextSend;
import nju.androidchat.client.component.OnRecallMessageRequested;

@Log
public class Mvp0TalkActivity extends AppCompatActivity implements Mvp0Contract.View, TextView.OnEditorActionListener, OnRecallMessageRequested {
    private Mvp0Contract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Mvp0TalkModel mvp0TalkModel = new Mvp0TalkModel();

        // Create the presenter
        this.presenter = new Mvp0TalkPresenter(mvp0TalkModel, this, new ArrayList<>());
        mvp0TalkModel.setIMvp0TalkPresenter(this.presenter);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void showMessageList(List<ClientMessage> messages) {
        runOnUiThread(() -> {
                    LinearLayout content = findViewById(R.id.chat_content);

                    // 删除所有已有的ItemText
                    content.removeAllViews();

                    // 增加ItemText
                    for (ClientMessage message : messages) {
                        String text = String.format("%s", message.getMessage());
                        // 如果是自己发的，增加ItemTextSend
                        if (message.getSenderUsername().equals(this.presenter.getUsername())) {
                            content.addView(new ItemTextSend(this, text, message.getMessageId(), this));
                        } else {
                            content.addView(new ItemTextReceive(this, text, message.getMessageId()));
                        }
                    }

                    Utils.scrollListToBottom(this);
                }
        );
    }

    @Override
    public void setPresenter(Mvp0Contract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            return hideKeyboard();
        }
        return super.onTouchEvent(event);
    }

    private boolean hideKeyboard() {
        return Utils.hideKeyboard(this);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (Utils.send(actionId, event)) {
            hideKeyboard();
            // 异步地让Controller处理事件
            sendText();
        }
        return false;
    }

    private void sendText() {
        EditText text = findViewById(R.id.et_content);
        AsyncTask.execute(() -> {
            this.presenter.sendMessage(text.getText().toString());
        });
    }

    public void onBtnSendClicked(View v) {
        hideKeyboard();
        sendText();
    }

    // 当用户长按消息，并选择撤回消息时做什么，MVP-0不实现
    @Override
    public void onRecallMessageRequested(UUID messageId) {

    }

    /**
     * 解析markdown文本并返回spanned
     *
     * @param text      源文本
     * @param imageGetter 图片获取回调
     * @param textView    textView
     * @return spanned
     */
    public static Spanned fromMarkdown(String text, Html.ImageGetter imageGetter, TextView textView) {
        MarkDownParser parser = new MarkDownParser(text, new StyleBuilderImpl(textView, imageGetter));
        try {
            return parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析markdown文本并返回spanned
     *
     * @param inputStream 输入流
     * @param imageGetter 图片获取回调
     * @param textView    textView
     * @return spanned
     */
    public static Spanned fromMarkdown(InputStream inputStream, Html.ImageGetter imageGetter, TextView textView) {
        MarkDownParser parser = new MarkDownParser(inputStream, new StyleBuilderImpl(textView, imageGetter));
        try {
            return parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析markdown文本并返回spanned
     *
     * @param reader      BufferReader
     * @param imageGetter 图片获取回调
     * @param textView    textView
     * @return spanned
     */
    public static Spanned fromMarkdown(BufferedReader reader, Html.ImageGetter imageGetter, TextView textView) {
        MarkDownParser parser = new MarkDownParser(reader, new StyleBuilderImpl(textView, imageGetter));
        try {
            return parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
