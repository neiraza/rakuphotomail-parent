package jp.co.fttx.rakuphotomail;

import android.os.Parcel;
import android.os.Parcelable;

public class GalleryMail implements Parcelable {
	private long messageId;
	private String messageUid;
	private String subject;
	private String preview;
	private long attachmentId;
	private String accountUuid;
	private String attachmentName;
	private String mimeType;

	public static final Parcelable.Creator<GalleryMail> CREATOR = new Parcelable.Creator<GalleryMail>() {
		@Override
		public GalleryMail createFromParcel(Parcel source) {
			GalleryMail mail = new GalleryMail();
			mail.messageId = source.readLong();
			mail.messageUid = source.readString();
			mail.subject = source.readString();
			mail.preview = source.readString();
			mail.attachmentId = source.readLong();
			mail.accountUuid = source.readString();
			mail.attachmentName = source.readString();
			mail.mimeType = source.readString();
			return mail;
		}

		@Override
		public GalleryMail[] newArray(int size) {
			return new GalleryMail[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(messageId);
		dest.writeString(messageUid);
		dest.writeString(subject);
		dest.writeString(preview);
		dest.writeLong(attachmentId);
		dest.writeString(accountUuid);
		dest.writeString(attachmentName);
		dest.writeString(mimeType);
	}

	public String toString() {
		return "messageId:" + messageId + " messageUid:" + messageUid + " subject:" + subject + " preview:"
				+ preview + " attachmentId:" + attachmentId + " accountUuid:" + accountUuid
				+ " attachmentName:" + attachmentName + " mimeType:" + mimeType;
	}

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public String getMessageUid() {
		return messageUid;
	}

	public void setMessageUid(String messageUid) {
		this.messageUid = messageUid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getAccountUuid() {
		return accountUuid;
	}

	public void setAccountUuid(String accountUuid) {
		this.accountUuid = accountUuid;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
