package jp.co.fttx.rakuphotomail.activity.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.RakuPhotoActivity;
import jp.co.fttx.rakuphotomail.mail.AuthenticationFailedException;
import jp.co.fttx.rakuphotomail.mail.CertificateValidationException;
import jp.co.fttx.rakuphotomail.mail.Store;
import jp.co.fttx.rakuphotomail.mail.Transport;
import jp.co.fttx.rakuphotomail.mail.filter.Hex;
import jp.co.fttx.rakuphotomail.mail.store.TrustManagerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

/**
 * Checks the given settings to make sure that they can be used to send and
 * receive mail.
 * <p/>
 * XXX NOTE: The manifest for this app has it ignore config changes, because
 * it doesn't correctly deal with restarting while its thread is running.
 */
public class AccountSetupCheckSettings extends RakuPhotoActivity implements OnClickListener {

    public static final int ACTIVITY_REQUEST_CODE = 1;

    private static final String EXTRA_ACCOUNT = "account";

    private static final String EXTRA_CHECK_INCOMING = "checkIncoming";

    private static final String EXTRA_CHECK_OUTGOING = "checkOutgoing";

    private Handler mHandler = new Handler();

    private ProgressBar mProgressBar;

    private TextView mMessageView;

    private Account mAccount;

    private boolean mCheckIncoming;

    private boolean mCheckOutgoing;

    private boolean mCanceled;

    private boolean mDestroyed;

    public static void actionCheckSettings(Activity context, Account account,
                                           boolean checkIncoming, boolean checkOutgoing) {
        Intent i = new Intent(context, AccountSetupCheckSettings.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_CHECK_INCOMING, checkIncoming);
        i.putExtra(EXTRA_CHECK_OUTGOING, checkOutgoing);
        context.startActivityForResult(i, ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.account_setup_check_settings);
        mMessageView = (TextView) findViewById(R.id.message);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        ((Button) findViewById(R.id.cancel)).setOnClickListener(this);

        setMessage(R.string.account_setup_check_settings_retr_info_msg);
        mProgressBar.setIndeterminate(true);

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        mCheckIncoming = getIntent().getBooleanExtra(EXTRA_CHECK_INCOMING, false);
        mCheckOutgoing = getIntent().getBooleanExtra(EXTRA_CHECK_OUTGOING, false);

        new Thread() {
            @Override
            public void run() {
                Store store = null;
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    if (mDestroyed) {
                        return;
                    }
                    if (mCanceled) {
                        finish();
                        return;
                    }
                    if (mDestroyed) {
                        return;
                    }
                    if (mCanceled) {
                        finish();
                        return;
                    }
                    if (mCheckIncoming) {
                        store = mAccount.getRemoteStore();
                        setMessage(R.string.account_setup_check_settings_check_incoming_msg);
                        store.checkSettings();
                    }
                    if (mCheckOutgoing) {
                        setMessage(R.string.account_setup_check_settings_check_outgoing_msg);
                        Transport transport = Transport.getInstance(mAccount);
                        transport.close();
                        transport.open();
                        transport.close();
                    }
                    if (mDestroyed) {
                        return;
                    }
                    if (mCanceled) {
                        finish();
                        return;
                    }
                    setResult(RESULT_OK);
                    finish();
                } catch (final AuthenticationFailedException afe) {
                    Log.e(RakuPhotoMail.LOG_TAG, "Error while testing settings", afe);
                    showErrorBackDialog(
                            R.string.account_setup_failed_dlg_auth_message_fmt,
                            afe.getMessage() == null ? "" : afe.getMessage());
                } catch (final CertificateValidationException cve) {
                    Log.e(RakuPhotoMail.LOG_TAG, "Error while testing settings", cve);
                    acceptKeyDialog(
                            R.string.account_setup_failed_dlg_certificate_message_fmt,
                            cve);
                } catch (final Throwable t) {
                    Log.e(RakuPhotoMail.LOG_TAG, "Error while testing settings", t);
                    showErrorBackDialog(
                            R.string.account_setup_failed_dlg_server_message_fmt,
                            (t.getMessage() == null ? "" : t.getMessage()));

                }
            }

        }
                .start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        mCanceled = true;
    }

    private void setMessage(final int resId) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                mMessageView.setText(getString(resId));
            }
        });
    }

    private void showErrorBackDialog(final int msgResId, final Object... args) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                mProgressBar.setIndeterminate(false);
                new AlertDialog.Builder(AccountSetupCheckSettings.this)
                        .setTitle(getString(R.string.account_setup_failed_dlg_title))
                        .setMessage(getString(msgResId, args))
                        .setCancelable(true)
                        .setPositiveButton(
                                getString(R.string.account_setup_failed_dlg_edit_action),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                        .show();
            }
        });
    }

    private void showErrorDialog(final int msgResId, final Object... args) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                mProgressBar.setIndeterminate(false);
                new AlertDialog.Builder(AccountSetupCheckSettings.this)
                        .setTitle(getString(R.string.account_setup_failed_dlg_title))
                        .setMessage(getString(msgResId, args))
                        .setCancelable(true)
                        .setNegativeButton(
                                getString(R.string.account_setup_failed_dlg_continue_action),

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        mCanceled = false;
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                })
                        .setPositiveButton(
                                getString(R.string.account_setup_failed_dlg_edit_details_action),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                        .show();
            }
        });
    }

    private void acceptKeyDialog(final int msgResId, final Object... args) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                final X509Certificate[] chain = TrustManagerFactory.getLastCertChain();
                String exMessage = "Unknown Error";

                Exception ex = ((Exception) args[0]);
                if (ex != null) {
                    if (ex.getCause() != null) {
                        if (ex.getCause().getCause() != null) {
                            exMessage = ex.getCause().getCause().getMessage();

                        } else {
                            exMessage = ex.getCause().getMessage();
                        }
                    } else {
                        exMessage = ex.getMessage();
                    }
                }

                mProgressBar.setIndeterminate(false);
                StringBuffer chainInfo = new StringBuffer(100);
                MessageDigest sha1 = null;
                try {
                    sha1 = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    Log.e(RakuPhotoMail.LOG_TAG, "Error while initializing MessageDigest", e);
                }
                for (int i = 0; i < chain.length; i++) {
                    // display certificate chain information
                    chainInfo.append("Certificate chain[" + i + "]:\n");
                    chainInfo.append("Subject: " + chain[i].getSubjectDN().toString() + "\n");

                    // display SubjectAltNames too
                    // (the user may be mislead into mistrusting a certificate
                    //  by a subjectDN not matching the server even though a
                    //  SubjectAltName matches)
                    try {
                        final Collection<List<?>> subjectAlternativeNames = chain[i].getSubjectAlternativeNames();
                        if (subjectAlternativeNames != null) {
                            // The list of SubjectAltNames may be very long
                            StringBuffer altNamesText = new StringBuffer("Subject has " + subjectAlternativeNames.size() + " alternative names\n");

                            // we need these for matching
                            String storeURIHost = (Uri.parse(mAccount.getStoreUri())).getHost();
                            String transportURIHost = (Uri.parse(mAccount.getTransportUri())).getHost();

                            for (List<?> subjectAlternativeName : subjectAlternativeNames) {
                                Integer type = (Integer) subjectAlternativeName.get(0);
                                Object value = subjectAlternativeName.get(1);
                                String name = "";
                                switch (type.intValue()) {
                                    case 0:
                                        Log.w(RakuPhotoMail.LOG_TAG, "SubjectAltName of type OtherName not supported.");
                                        continue;
                                    case 1: // RFC822Name
                                        name = (String) value;
                                        break;
                                    case 2:  // DNSName
                                        name = (String) value;
                                        break;
                                    case 3:
                                        Log.w(RakuPhotoMail.LOG_TAG, "unsupported SubjectAltName of type x400Address");
                                        continue;
                                    case 4:
                                        Log.w(RakuPhotoMail.LOG_TAG, "unsupported SubjectAltName of type directoryName");
                                        continue;
                                    case 5:
                                        Log.w(RakuPhotoMail.LOG_TAG, "unsupported SubjectAltName of type ediPartyName");
                                        continue;
                                    case 6:  // Uri
                                        name = (String) value;
                                        break;
                                    case 7: // ip-address
                                        name = (String) value;
                                        break;
                                    default:
                                        Log.w(RakuPhotoMail.LOG_TAG, "unsupported SubjectAltName of unknown type");
                                        continue;
                                }

                                // if some of the SubjectAltNames match the store or transport -host,
                                // display them
                                if (name.equalsIgnoreCase(storeURIHost) || name.equalsIgnoreCase(transportURIHost)) {
                                    altNamesText.append("Subject(alt): " + name + ",...\n");
                                } else if (name.startsWith("*.")) {
                                    if (storeURIHost.endsWith(name.substring(2)) || transportURIHost.endsWith(name.substring(2))) {
                                        altNamesText.append("Subject(alt): " + name + ",...\n");
                                    }
                                }
                            }
                            chainInfo.append(altNamesText);
                        }
                    } catch (Exception e1) {
                        // don't fail just because of subjectAltNames
                        Log.w(RakuPhotoMail.LOG_TAG, "cannot display SubjectAltNames in dialog", e1);
                    }

                    chainInfo.append("Issuer: " + chain[i].getIssuerDN().toString() + "\n");
                    if (sha1 != null) {
                        sha1.reset();
                        try {
                            char[] sha1sum = Hex.encodeHex(sha1.digest(chain[i].getEncoded()));
                            chainInfo.append("Fingerprint (SHA-1): " + new String(sha1sum) + "\n");
                        } catch (CertificateEncodingException e) {
                            Log.e(RakuPhotoMail.LOG_TAG, "Error while encoding certificate", e);
                        }
                    }
                }

                new AlertDialog.Builder(AccountSetupCheckSettings.this)
                        .setTitle(getString(R.string.account_setup_failed_dlg_invalid_certificate_title))
                                //.setMessage(getString(R.string.account_setup_failed_dlg_invalid_certificate)
                        .setMessage(getString(msgResId, exMessage)
                                + " " + chainInfo.toString()
                        )
                        .setCancelable(true)
                        .setPositiveButton(
                                getString(R.string.account_setup_failed_dlg_invalid_certificate_accept),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            String alias = mAccount.getUuid();
                                            if (mCheckIncoming) {
                                                alias = alias + ".incoming";
                                            }
                                            if (mCheckOutgoing) {
                                                alias = alias + ".outgoing";
                                            }
                                            TrustManagerFactory.addCertificateChain(alias, chain);
                                        } catch (CertificateException e) {
                                            showErrorDialog(
                                                    R.string.account_setup_failed_dlg_certificate_message_fmt,
                                                    e.getMessage() == null ? "" : e.getMessage());
                                        }
                                        AccountSetupCheckSettings.actionCheckSettings(AccountSetupCheckSettings.this, mAccount,
                                                mCheckIncoming, mCheckOutgoing);
                                    }
                                })
                        .setNegativeButton(
                                getString(R.string.account_setup_failed_dlg_invalid_certificate_reject),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                        .show();
            }
        });
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        setResult(resCode);
        finish();
    }


    private void onCancel() {
        mCanceled = true;
        setMessage(R.string.account_setup_check_settings_canceling_msg);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                onCancel();
                break;
        }
    }
}
