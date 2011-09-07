package jp.co.fttx.rakuphotomail.rakuraku.bean;

import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalAttachmentBodyPart;

public class AttachmentBean {
	private long id;
	private String name;
	private int size;
	private String type;
	private String mimeType;
	private LocalAttachmentBodyPart part;

	public LocalAttachmentBodyPart getPart() {
		return part;
	}

	public void setPart(LocalAttachmentBodyPart part) {
		this.part = part;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
