package jp.co.fttx.rakuphotomail.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import jp.co.fttx.rakuphotomail.*;

import java.util.List;

/**
 * Activity displaying list of accounts/identity for user choice
 * 
 * @see jp.co.fttx.rakuphotomail.activity.RakuphotoExpandableListActivity
 */
public class BKUPChooseAccount extends RakuphotoExpandableListActivity {

	/**
	 * {@link android.content.Intent} extended data name for storing {@link jp.co.fttx.rakuphotomail.Account#getUuid()
	 * account UUID}
	 */
	public static final String EXTRA_ACCOUNT = BKUPChooseAccount.class.getName()
			+ "_account";

	/**
	 * {@link android.content.Intent} extended data name for storing serialized {@link jp.co.fttx.rakuphotomail.Identity}
	 */
	public static final String EXTRA_IDENTITY = BKUPChooseAccount.class.getName()
			+ "_identity";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.choose_account);

		final ExpandableListView expandableListView = getExpandableListView();
		expandableListView.setItemsCanFocus(false);

		final ExpandableListAdapter adapter = createAdapter();
		setListAdapter(adapter);

		expandableListView
				.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
					@Override
					public boolean onChildClick(ExpandableListView parent,
							View v, int groupPosition, int childPosition,
							long id) {
						final Identity identity = (Identity) adapter.getChild(
								groupPosition, childPosition);
						final Account account = (Account) adapter
								.getGroup(groupPosition);

						if (!account.isAvailable(v.getContext())) {
							Log.i(RakuPhotoMail.LOG_TAG,
									"Refusing selection of unavailable account");
							return true;
						}
						final Intent intent = new Intent();
						intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
						intent.putExtra(EXTRA_IDENTITY, identity);
						setResult(RESULT_OK, intent);

						finish();
						return true;
					}
				});

		final Bundle extras = getIntent().getExtras();
		final String uuid = extras.getString(EXTRA_ACCOUNT);
		if (uuid != null) {
			final Account[] accounts = Preferences.getPreferences(this)
					.getAccounts();
			final int length = accounts.length;
			for (int i = 0; i < length; i++) {
				final Account account = accounts[i];
				if (uuid.equals(account.getUuid())) {
					// setSelectedChild() doesn't seem to obey the
					// shouldExpandGroup parameter (2.1), manually expanding
					// group
					expandableListView.expandGroup(i);

					final List<Identity> identities = account.getIdentities();
					final Identity identity = (Identity) extras
							.getSerializable(EXTRA_IDENTITY);
					if (identity == null) {
						expandableListView.setSelectedChild(i, 0, true);
						break;
					}
					for (int j = 0; j < identities.size(); j++) {
						final Identity loopIdentity = identities.get(j);
						if (identity.equals(loopIdentity)) {
							expandableListView.setSelectedChild(i, j, true);
							break;
						}
					}
					break;
				}
			}
		}
	}

	private ExpandableListAdapter createAdapter() {
		return new IdentitiesAdapter(this, getLayoutInflater());
	}

	/**
	 * Dynamically provides accounts/identities data for
	 * {@link android.widget.ExpandableListView#setAdapter(android.widget.ExpandableListAdapter)}:
	 *
	 * <ul>
	 * <li>Groups represent {@link jp.co.fttx.rakuphotomail.Account accounts}</li>
	 * <li>Children represent {@link jp.co.fttx.rakuphotomail.Identity identities} of the parent account</li>
	 * </ul>
	 */
	public static class IdentitiesAdapter extends BaseExpandableListAdapter {

		private Context mContext;
		private LayoutInflater mLayoutInflater;

		public IdentitiesAdapter(final Context context,
				final LayoutInflater layoutInflater) {
			mContext = context;
			mLayoutInflater = layoutInflater;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return getAccounts()[groupPosition].getIdentity(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return Integer.valueOf(childPosition).longValue();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return getAccounts()[groupPosition].getIdentities().size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return getAccounts()[groupPosition];
		}

		@Override
		public int getGroupCount() {
			return getAccounts().length;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return Integer.valueOf(groupPosition).longValue();
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			final View v;
			if (convertView == null) {
				v = mLayoutInflater.inflate(R.layout.choose_account_item,
						parent, false);
			} else {
				v = convertView;
			}

			final TextView description = (TextView) v
					.findViewById(R.id.description);
			final Account account = getAccounts()[groupPosition];
			description.setText(account.getDescription());
			description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, RakuPhotoMail
					.getFontSizes().getAccountName());

			// display unavailable accounts translucent
			/*
			 * 20101030/fiouzy: NullPointerException on null getBackground()
			 * 
			 * if (account.isAvailable(parent.getContext())) {
			 * description.getBackground().setAlpha(255);
			 * description.getBackground().setAlpha(255); } else {
			 * description.getBackground().setAlpha(127);
			 * description.getBackground().setAlpha(127); }
			 */

			v.findViewById(R.id.chip)
					.setBackgroundColor(account.getChipColor());

			return v;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			final Account account = getAccounts()[groupPosition];
			final Identity identity = account.getIdentity(childPosition);

			final View v;
			if (convertView == null) {
				v = mLayoutInflater.inflate(R.layout.choose_identity_item,
						parent, false);
			} else {
				v = convertView;
			}

			final TextView name = (TextView) v.findViewById(R.id.name);
			final TextView description = (TextView) v
					.findViewById(R.id.description);
			name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, RakuPhotoMail.getFontSizes()
					.getAccountName());
			description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, RakuPhotoMail
					.getFontSizes().getAccountDescription());

			name.setText(identity.getDescription());
			description.setText(String.format("%s <%s>", identity.getName(),
					identity.getEmail()));

			v.findViewById(R.id.chip)
					.setBackgroundColor(account.getChipColor());

			return v;
		}

		@Override
		public boolean hasStableIds() {
			// returning false since accounts/identities are mutable
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		private Account[] getAccounts() {
			return Preferences.getPreferences(mContext).getAccounts();
		}
	}
}
