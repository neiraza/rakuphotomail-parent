/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.test;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.activity.GallerySendingMail;
import jp.co.fttx.rakuphotomail.activity.MessageReference;
import jp.co.fttx.rakuphotomail.mail.Flag;

/**
 * jp.co.fttx.rakuphotomail.GallerySendingMailTest.
 *
 * @author tooru.oguri
 * @since 0.1-beta1
 */
public class GallerySendingMailTest extends
        ActivityInstrumentationTestCase2<GallerySendingMail> {

    private GallerySendingMail mActivity;
    private EditText mMailContent;
    private TextView mToAddressAndName;
    private TextView mSentFlag;
    private Button mSend;
    private static final String EXTRA_ADDRESS_FROM = "addressFrom";
    private static final String EXTRA_ADDRESS_FROM_NAME = "addressFromName";
    private static final String EXTRA_ADDRESS_TO = "addressTo";
    private static final String EXTRA_ADDRESS_TO_NAME = "addressToName";
    private static final String EXTRA_ADDRESS_REPLY_TO = "addressReplyTo";
    private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
    private static final String EXTRA_MESSAGE_ANSWERED = "answered";
    MessageReference mReference;

    private Account mAccount;

    public GallerySendingMailTest() {
        super("jp.co.fttx.rakuphotomail.activity", GallerySendingMail.class);
    }

    /**
     * @author tooru.oguri
     * @see android.test.ActivityInstrumentationTestCase2#setUp()
     * @since 0.1-beta1
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = null;
        mMailContent = null;
        mToAddressAndName = null;
        mSentFlag = null;
        mSend = null;
    }

    /**
     * @author tooru.oguri
     * @see android.test.ActivityInstrumentationTestCase2#tearDown()
     * @since 0.1-beta1
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * null check(mMailContent).
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testNotNullMMailContent() {
        startActivity();
        setUpViews();

        assertNotNull(mMailContent);
    }

    /**
     * null check(mToAddressAndName).
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testNotNullMTo() {
        startActivity();
        setUpViews();

        assertNotNull(mToAddressAndName);
    }

    /**
     * null check(mSend).
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testNotNullMSend() {
        startActivity();
        setUpViews();

        assertNotNull(mSend);
    }

    /**
     * null check(mSentFlag).
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testNotNullMSentFlag() {
        startActivity();
        setUpViews();

        assertNotNull(mSentFlag);
    }

    /**
     * 未入力チェック（mMailContent）.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testInitialInput() {
        startActivity();
        setUpViews();

        assertEquals("", mMailContent.getText().toString());
    }

    /**
     * 未入力チェック（mMailContent）.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testInitialInputHint() {
        startActivity();
        setUpViews();

        assertEquals("ここにメッセージを入力して下さい", mMailContent.getHint().toString());
    }

    /**
     * 宛先名表示内容チェック.<br>
     * 宛先名が想定通りの文言で表示されるか確認する.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testToName() {
        startActivity();
        setUpViews();

        assertEquals("Miyamoto(shigeharu.miyamoto@rakuphoto.ucom.local)", mToAddressAndName.getText().toString());
    }


    /**
     * 返信済みフラグ非表示チェック（返信無し）.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testReplySentFlagOff() {
        startActivity();
        setUpViews();

        assertEquals(View.GONE, mSentFlag.getVisibility());
    }

    /**
     * 返信済みフラグ表示文言チェック（返信有り）.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testReplySentFlagOnStr() throws InterruptedException {

        startActivitySentFlagOn();
        setUpViews();

        assertEquals("返信済み！", mSentFlag.getText().toString());
    }

    /**
     * 返信済みフラグ表示チェック（返信有り）.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testReplySentFlagOn() throws InterruptedException {

        startActivitySentFlagOn();
        setUpViews();

        assertEquals(View.VISIBLE, mSentFlag.getVisibility());
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testReplyNG() throws InterruptedException {
        try {
            startActivity();
            setUpViews();

            assertTrue(mSend.isEnabled());

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSend.requestFocus();
                }
            });
            getInstrumentation().waitForIdleSync();

            sendKeys(KeyEvent.KEYCODE_ENTER);
            getInstrumentation().waitForIdleSync();

            sendKeys(KeyEvent.KEYCODE_BACK);
            getInstrumentation().waitForIdleSync();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * 文字入力チェック（正常、入力可能文字数以下）.<br>
     * 但し、使用するテスト環境（エミュレータ、実機）によっては、<br>
     * 日本語使用が不可能な為、失敗する可能性がある（どうしようもない）
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testReplyMailSizeOK() throws InterruptedException {
        startActivity();
        setUpViews();

        assertTrue(mSend.isEnabled());

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMailContent.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_0);
        getInstrumentation().waitForIdleSync();

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSend.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_ENTER);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_BACK);
        getInstrumentation().waitForIdleSync();
    }

    /**
     * 文字入力チェック（正常、入力可能文字数（17文字））.<br>
     * 但し、使用するテスト環境（エミュレータ、実機）によっては、<br>
     * 日本語使用が不可能な為、失敗する可能性がある（どうしようもない）
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public void testReplyMailSizeNG() throws InterruptedException {
        startActivity();
        setUpViews();

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMailContent.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();

        input18();

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSend.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_ENTER);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_BACK);
        getInstrumentation().waitForIdleSync();

    }

    /**
     * 文字入力チェック（異常、未入力時）.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
//    public void testReplyMailSubjectNoInput() throws InterruptedException {
//
//    startActivity();
//    setUpViews();
//
//    // 入力確認
//    assertEquals("", mMailContent.getText().toString());
//
//    mActivity.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        mSend.performClick();
//      }
//    });
//    mInstrumentation.waitForIdleSync();
//
//    }

    /**
     * 文字入力チェック（異常、入力可能文字数超過時）.<br>
     * 但し、使用するテスト環境（エミュレータ、実機）によっては、<br>
     * 日本語使用が不可能な為、失敗する可能性がある（どうしようもない）
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
//    public void testReplyMailSubjectSizeOVER() throws InterruptedException {
//
//    startActivity();
//    setUpViews();
//
//    mActivity.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        mMailContent.requestFocus();
//      }
//    });
//    mInstrumentation.waitForIdleSync();
//
//    sendKeys(KEYCODE_A, KEYCODE_I, KEYCODE_F, KEYCODE_O, KEYCODE_N,
//        KEYCODE_N, KEYCODE_A, KEYCODE_I, KEYCODE_F, KEYCODE_O,
//        KEYCODE_N, KEYCODE_N, KEYCODE_A, KEYCODE_I, KEYCODE_F,
//        KEYCODE_O, KEYCODE_N, KEYCODE_N);
//
//    // 入力確認
//    assertEquals("あいふぉんあいふぉんあいふぉん", mMailContent.getText().toString());
//
//    mActivity.runOnUiThread(new Runnable() {
//      @Override
//      public void run() {
//        mSend.performClick();
//      }
//    });
//    mInstrumentation.waitForIdleSync();
//
//    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void startActivity() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.putExtra(EXTRA_ADDRESS_FROM, "tooru.oguri@rakuphoto.ucom.local");
        i.putExtra(EXTRA_ADDRESS_FROM_NAME, "Togu");
        i.putExtra(EXTRA_ADDRESS_TO, "shigeharu.miyamoto@rakuphoto.ucom.local");
        i.putExtra(EXTRA_ADDRESS_TO_NAME, "Miyamoto");
        i.putExtra(EXTRA_ADDRESS_REPLY_TO,
                "shigeharu.miyamoto@rakuphoto.ucom.local");
        i.putExtra(EXTRA_MESSAGE_ANSWERED, false);

        mReference = new MessageReference();
        mReference.accountUuid = "a848514f-b6a4-447f-9f8a-8632cd9c8316";
        mReference.folderName = "INBOX";
        mReference.uid = "500";
        mReference.flag = Flag.ANSWERED;
        i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
        setActivityIntent(i);

        mActivity = getActivity();
        createAccount();
    }

    private void createAccount() {
        mAccount = Preferences.getPreferences(mActivity).newAccount();
        mAccount.setName("toguri");
        mAccount.setEmail("tooru.oguri@rakuphoto.ucom.local");
        mAccount.setStoreUri("172.29.51.226");
        mAccount.setTransportUri("172.29.51.226");
        mAccount.setDraftsFolderName(mActivity.getString(R.string.special_mailbox_name_drafts));
        mAccount.setTrashFolderName(mActivity.getString(R.string.special_mailbox_name_trash));
        mAccount.setArchiveFolderName(mActivity.getString(R.string.special_mailbox_name_archive));
        mAccount.setSpamFolderName(mActivity.getString(R.string.special_mailbox_name_spam));
        mAccount.setSentFolderName(mActivity.getString(R.string.special_mailbox_name_sent));
        mAccount.save(Preferences.getPreferences(mActivity));
    }

//    /**
//     * @author tooru.oguri
//     * @since 0.1-beta1
//     */
//    private void startActivityDefaultTo() {
//        Intent i = new Intent(Intent.ACTION_MAIN);
//        i.putExtra(EXTRA_ADDRESS_FROM, "tooru.oguri@rakuphoto.ucom.local");
//        i.putExtra(EXTRA_ADDRESS_FROM_NAME, "Togu");
//        i.putExtra(EXTRA_ADDRESS_TO, "shigeharu.miyamoto@rakuphoto.ucom.local");
//        i.putExtra(EXTRA_ADDRESS_TO_NAME, "");
//        i.putExtra(EXTRA_ADDRESS_REPLY_TO,
//                "shigeharu.miyamoto@rakuphoto.ucom.local");
//        i.putExtra(EXTRA_MESSAGE_ANSWERED, false);
//
//        mReference = new MessageReference();
//        mReference.accountUuid = "a848514f-b6a4-447f-9f8a-8632cd9c8316";
//        mReference.folderName = "INBOX";
//        mReference.uid = "500";
//        mReference.flag = Flag.ANSWERED;
//        i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
//        setActivityIntent(i);
//
//        mActivity = getActivity();
//    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void startActivitySentFlagOn() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.putExtra(EXTRA_ADDRESS_FROM, "tooru.oguri@rakuphoto.ucom.local");
        i.putExtra(EXTRA_ADDRESS_FROM_NAME, "Togu");
        i.putExtra(EXTRA_ADDRESS_TO, "shigeharu.miyamoto@rakuphoto.ucom.local");
        i.putExtra(EXTRA_ADDRESS_TO_NAME, "Miyamoto");
        i.putExtra(EXTRA_ADDRESS_REPLY_TO,
                "shigeharu.miyamoto@rakuphoto.ucom.local");
        i.putExtra(EXTRA_MESSAGE_ANSWERED, true);

        mReference = new MessageReference();
        mReference.accountUuid = "a848514f-b6a4-447f-9f8a-8632cd9c8316";
        mReference.folderName = "INBOX";
        mReference.uid = "500";
        mReference.flag = Flag.ANSWERED;
        i.putExtra(EXTRA_MESSAGE_REFERENCE, mReference);
        setActivityIntent(i);

        mActivity = getActivity();
        createAccount();

    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void setUpViews() {
        mMailContent = (EditText) mActivity
                .findViewById(R.id.gallery_sending_mail_content);
        mSend = (Button) mActivity.findViewById(R.id.gallery_sending_mail_send);
        mToAddressAndName = (TextView) mActivity
                .findViewById(R.id.gallery_sending_mail_to_address_and_name);
        mSentFlag = (TextView) mActivity
                .findViewById(R.id.gallery_sending_mail_sent_flag);
    }

    private void input18() {
        sendKeys(KeyEvent.KEYCODE_0);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_1);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_2);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_3);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_4);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_5);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_6);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_7);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_8);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_9);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_0);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_1);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_2);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_3);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_4);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_5);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_6);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_7);
        getInstrumentation().waitForIdleSync();

        sendKeys(KeyEvent.KEYCODE_8);
        getInstrumentation().waitForIdleSync();
    }

}
