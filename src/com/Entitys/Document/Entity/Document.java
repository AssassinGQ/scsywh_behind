package com.Entitys.Document.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name="document")
public class Document extends Bean {
	@Column(name="title_", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String title;
	@Column(name="desc_", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String desc;
	@Column(name="content_")
	@Valid(needValid = false)
	private String content;
	@Column(name="iconsid", length=10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long iconsid;
	@Column(name="imagesid", length=10)
	@Valid(nullAble = true, varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long imagesid;
	public Document() {
		super();
	}
	public Document(Long corporationsid, String title, String desc, String content) {
		super();
		this.corporationsid = corporationsid;
		this.title = title;
		this.desc = desc;
		this.content = content;
		this.iconsid = null;
		this.imagesid = null;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Long getIconsid() {
		return iconsid;
	}
	public void setIconsid(Long iconsid) {
		this.iconsid = iconsid;
	}
	public Long getImagesid() {
		return imagesid;
	}
	public void setImagesid(Long imagesid) {
		this.imagesid = imagesid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
