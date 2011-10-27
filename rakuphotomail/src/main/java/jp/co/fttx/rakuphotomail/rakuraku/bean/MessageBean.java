package jp.co.fttx.rakuphotomail.rakuraku.bean;

import java.util.concurrent.CopyOnWriteArrayList;

import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalMessage;

public class MessageBean {
	private long id;
	private long folderId;
	private String uid;
	private String subject;
	private long date;
	private String senderList;
	private String senderListName;
	private String toList;
	private String toListName;
	private String ccList;
	private String bccList;
	private String replyToList;
	private String messageId;
	private String textContent;
	private long attachmentCount;
	private CopyOnWriteArrayList<AttachmentBean> attachments;
	private LocalMessage message;
	private String flags;
	private boolean flagXGotAllHeaders;
	private boolean flagSeen;
	private boolean flagAnswered;
	private boolean flagXDownLoadedFull;
	private boolean flagXDownLoadedPartial;
	private boolean flagXRemoteCopyStarted;
	private String flagOther;

	public MessageBean() {
		attachments = new CopyOnWriteArrayList<AttachmentBean>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFolderId() {
		return folderId;
	}

	public void setFolderId(long folderId) {
		this.folderId = folderId;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getSenderList() {
		return senderList;
	}

	public void setSenderList(String senderList) {
		this.senderList = senderList;
	}

	public String getToList() {
		return toList;
	}

	public void setToList(String toList) {
		this.toList = toList;
	}

	public String getCcList() {
		return ccList;
	}

	public void setCcList(String ccList) {
		this.ccList = ccList;
	}

	public String getBccList() {
		return bccList;
	}

	public void setBccList(String bccList) {
		this.bccList = bccList;
	}

	public String getReplyToList() {
		return replyToList;
	}

	public void setReplyToList(String replyToList) {
		this.replyToList = replyToList;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public long getAttachmentCount() {
		return attachmentCount;
	}

	public void setAttachmentCount(long attachmentCount) {
		this.attachmentCount = attachmentCount;
	}

	public CopyOnWriteArrayList<AttachmentBean> getAttachments() {
		return attachments;
	}

	public void setAttachments(CopyOnWriteArrayList<AttachmentBean> attachments) {
		this.attachments = attachments;
	}

	public LocalMessage getMessage() {
		return message;
	}

	public void setMessage(LocalMessage message) {
		this.message = message;
	}
	
	public String getSenderListName() {
		return senderListName;
	}

	public void setSenderListName(String senderListName) {
		this.senderListName = senderListName;
	}

	public String getToListName() {
		return toListName;
	}

	public void setToListName(String toListName) {
		this.toListName = toListName;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public boolean isFlagXGotAllHeaders() {
		return flagXGotAllHeaders;
	}

	public void setFlagXGotAllHeaders(boolean flagXGotAllHeaders) {
		this.flagXGotAllHeaders = flagXGotAllHeaders;
	}

	public boolean isFlagSeen() {
		return flagSeen;
	}

	public void setFlagSeen(boolean flagSeen) {
		this.flagSeen = flagSeen;
	}

	public boolean isFlagAnswered() {
		return flagAnswered;
	}

	public void setFlagAnswered(boolean flagAnswered) {
		this.flagAnswered = flagAnswered;
	}

	public boolean isFlagXDownLoadedFull() {
		return flagXDownLoadedFull;
	}

	public void setFlagXDownLoadedFull(boolean flagXDownLoadedFull) {
		this.flagXDownLoadedFull = flagXDownLoadedFull;
	}

	public boolean isFlagXDownLoadedPartial() {
		return flagXDownLoadedPartial;
	}

	public void setFlagXDownLoadedPartial(boolean flagXDownLoadedPartial) {
		this.flagXDownLoadedPartial = flagXDownLoadedPartial;
	}

	public boolean isFlagXRemoteCopyStarted() {
		return flagXRemoteCopyStarted;
	}

	public void setFlagXRemoteCopyStarted(boolean flagXRemoteCopyStarted) {
		this.flagXRemoteCopyStarted = flagXRemoteCopyStarted;
	}

	public String getFlagOther() {
		return flagOther;
	}

	public void setFlagOther(String flagOther) {
		this.flagOther = flagOther;
	}

}
