package jp.co.fttx.rakuphotomail.helper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.CommonDataKinds.Email;
import jp.co.fttx.rakuphotomail.mail.Address;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;

/**
 * Access the contacts on the device using the API introduced with SDK 5.
 *
 * @see android.provider.ContactsContract
 */
public class ContactsSdk5 extends jp.co.fttx.rakuphotomail.helper.Contacts {
    /**
     * The order in which the search results are returned by
     * {@link #searchContacts(CharSequence)}.
     */
    protected static final String SORT_ORDER =
        Email.TIMES_CONTACTED + " DESC, " +
        Contacts.DISPLAY_NAME + ", " +
        Email._ID;

    /**
     * Array of columns to load from the database.
     *
     * Important: The _ID field is needed by
     * {@link jp.co.fttx.rakuphotomail.EmailAddressAdapter} or more specificly by
     * {@link android.widget.ResourceCursorAdapter}.
     */
    protected static final String PROJECTION[] = {
        Email._ID,
        Contacts.DISPLAY_NAME,
        Email.DATA,
        Email.CONTACT_ID
    };

    /**
     * Index of the name field in the projection. This must match the order in
     * {@link #PROJECTION}.
     */
    protected static final int NAME_INDEX = 1;

    /**
     * Index of the email address field in the projection. This must match the
     * order in {@link #PROJECTION}.
     */
    protected static final int EMAIL_INDEX = 2;

    /**
     * Index of the contact id field in the projection. This must match the order in
     * {@link #PROJECTION}.
     */
    protected static final int CONTACT_ID_INDEX = 3;


    public ContactsSdk5(final Context context) {
        super(context);
    }

    @Override
    public void createContact(final Address email) {
        final Uri contactUri = Uri.fromParts("mailto", email.getAddress(), null);

        final Intent contactIntent = new Intent(Intents.SHOW_OR_CREATE_CONTACT);
        contactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contactIntent.setData(contactUri);

        // Pass along full E-mail string for possible create dialog
        contactIntent.putExtra(Intents.EXTRA_CREATE_DESCRIPTION,
                               email.toString());

        // Only provide personal name hint if we have one
        final String senderPersonal = email.getPersonal();
        if (senderPersonal != null) {
            contactIntent.putExtra(Intents.Insert.NAME, senderPersonal);
        }

        mContext.startActivity(contactIntent);
    }

    @Override
    public boolean isInContacts(final String emailAddress) {
        boolean result = false;

        final Cursor c = getContactByAddress(emailAddress);

        if (c != null) {
            if (c.getCount() > 0) {
                result = true;
            }
            c.close();
        }

        return result;
    }

    @Override
    public Cursor searchContacts(final CharSequence constraint) {
        final String filter = (constraint == null) ? "" : constraint.toString();
        final Uri uri = Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(filter));
        final Cursor c = mContentResolver.query(
                             uri,
                             PROJECTION,
                             null,
                             null,
                             SORT_ORDER);

        if (c != null) {
            /*
             * To prevent expensive execution in the UI thread:
             * Cursors get lazily executed, so if you don't call anything on
             * the cursor before returning it from the background thread you'll
             * have a complied program for the cursor, but it won't have been
             * executed to generate the data yet. Often the execution is more
             * expensive than the compilation...
             */
            c.getCount();
        }

        return c;
    }

    @Override
    public String getNameForAddress(String address) {
        if (address == null) {
            return null;
        }

        final Cursor c = getContactByAddress(address);

        String name = null;
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                name = getName(c);
            }
            c.close();
        }

        return name;
    }

    @Override
    public String getName(Cursor c) {
        return c.getString(NAME_INDEX);
    }

    @Override
    public String getEmail(Cursor c) {
        return c.getString(EMAIL_INDEX);
    }

    @Override
    public void markAsContacted(final Address[] addresses) {
        for (final Address address : addresses) {
            final Cursor c = getContactByAddress(address.getAddress());

            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    final long personId = c.getLong(CONTACT_ID_INDEX);
                    ContactsContract.Contacts.markAsContacted(mContentResolver, personId);
                }
                c.close();
            }
        }
    }

    @Override
    public Intent contactPickerIntent() {
        return new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
    }

    @Override
    public String getEmailFromContactPicker(final Intent data) {
        Cursor cursor = null;
        String email = "";

        try {
            Uri result = data.getData();

            // Get the contact id from the Uri
            String id = result.getLastPathSegment();
            cursor = mContentResolver.query(Email.CONTENT_URI,
                                            null, Email.CONTACT_ID + "=?", new String[] { id },
                                            null);

            int emailIdx = cursor.getColumnIndex(Email.DATA);

            if (cursor.moveToFirst()) {
                email = cursor.getString(emailIdx);
            }
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Failed to get email data", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return email;
    }

    /**
     * Return a {@link Cursor} instance that can be used to fetch information
     * about the contact with the given email address.
     *
     * @param address The email address to search for.
     * @return A {@link Cursor} instance that can be used to fetch information
     *         about the contact with the given email address
     */
    private Cursor getContactByAddress(final String address) {
        final Uri uri = Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(address));
        final Cursor c = mContentResolver.query(
                             uri,
                             PROJECTION,
                             null,
                             null,
                             SORT_ORDER);
        return c;
    }
}
