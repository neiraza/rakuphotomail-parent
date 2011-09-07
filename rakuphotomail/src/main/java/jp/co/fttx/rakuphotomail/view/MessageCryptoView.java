package jp.co.fttx.rakuphotomail.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.*;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.crypto.CryptoProvider;
import jp.co.fttx.rakuphotomail.crypto.PgpData;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.Part;
import jp.co.fttx.rakuphotomail.mail.internet.MimeUtility;




public class MessageCryptoView extends LinearLayout {

    private Context mContext;
    private Activity mActivity;
    private Button mDecryptButton;
    private LinearLayout mCryptoSignatureLayout = null;
    private ImageView mCryptoSignatureStatusImage = null;
    private TextView mCryptoSignatureUserId = null;
    private TextView mCryptoSignatureUserIdRest = null;


    public MessageCryptoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setupChildViews() {
        mCryptoSignatureLayout = (LinearLayout) findViewById(R.id.crypto_signature);
        mCryptoSignatureStatusImage = (ImageView) findViewById(R.id.ic_crypto_signature_status);
        mCryptoSignatureUserId = (TextView) findViewById(R.id.userId);
        mCryptoSignatureUserIdRest = (TextView) findViewById(R.id.userIdRest);
        mCryptoSignatureLayout.setVisibility(View.INVISIBLE);
        mDecryptButton = (Button) findViewById(R.id.btn_decrypt);
    }

    public void setActivity(Activity activity) {
        mActivity = activity;

    }


    public void hide() {
        this.setVisibility(View.GONE);
    }

    /**
     * Fill the decrypt layout with signature data, if known, make controls visible, if
     * they should be visible.
     */
    public void updateLayout(final CryptoProvider cryptoProvider, final PgpData pgpData, final Message message) {
        if (pgpData.getSignatureKeyId() != 0) {
            mCryptoSignatureUserIdRest.setText(
                mContext.getString(R.string.key_id, Long.toHexString(pgpData.getSignatureKeyId() & 0xffffffffL)));
            String userId = pgpData.getSignatureUserId();
            if (userId == null) {
                userId = mContext.getString(R.string.unknown_crypto_signature_user_id);
            }
            String chunks[] = userId.split(" <", 2);
            String name = chunks[0];
            if (chunks.length > 1) {
                mCryptoSignatureUserIdRest.setText("<" + chunks[1]);
            }
            mCryptoSignatureUserId.setText(name);
            if (pgpData.getSignatureSuccess()) {
                mCryptoSignatureStatusImage.setImageResource(R.drawable.overlay_ok);
            } else if (pgpData.getSignatureUnknown()) {
                mCryptoSignatureStatusImage.setImageResource(R.drawable.overlay_error);
            } else {
                mCryptoSignatureStatusImage.setImageResource(R.drawable.overlay_error);
            }
            mCryptoSignatureLayout.setVisibility(View.VISIBLE);
            this.setVisibility(View.VISIBLE);
        } else {
            mCryptoSignatureLayout.setVisibility(View.INVISIBLE);
        }
        if ((message == null) && (pgpData.getDecryptedData() == null)) {
            this.setVisibility(View.GONE);
            return;
        }
        if (pgpData.getDecryptedData() != null) {
            if (pgpData.getSignatureKeyId() == 0) {
                this.setVisibility(View.GONE);
            } else {
                // no need to show this after decryption/verification
                mDecryptButton.setVisibility(View.GONE);
            }
            return;
        }


        mDecryptButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String data = null;
                    Part part = MimeUtility.findFirstPartByMimeType(message, "text/plain");
                    if (part == null) {
                        part = MimeUtility.findFirstPartByMimeType(message, "text/html");
                    }
                    if (part != null) {
                        data = MimeUtility.getTextFromPart(part);
                    }
                    cryptoProvider.decrypt(mActivity, data, pgpData);
                } catch (MessagingException me) {
                    Log.e(RakuPhotoMail.LOG_TAG, "Unable to decrypt email.", me);
                }
            }
        });


        mDecryptButton.setVisibility(View.VISIBLE);
        if (cryptoProvider.isEncrypted(message)) {
            mDecryptButton.setText(R.string.btn_decrypt);
            this.setVisibility(View.VISIBLE);
        } else if (cryptoProvider.isSigned(message)) {
            mDecryptButton.setText(R.string.btn_verify);
            this.setVisibility(View.VISIBLE);
        } else {
            this.setVisibility(View.GONE);
            try {
                // check for PGP/MIME encryption
                Part pgp = MimeUtility.findFirstPartByMimeType(message, "application/pgp-encrypted");
                if (pgp != null) {
                    Toast.makeText(mContext, R.string.pgp_mime_unsupported, Toast.LENGTH_LONG).show();
                }
            } catch (MessagingException e) {
                // nothing to do...
            }
        }
    }

}
