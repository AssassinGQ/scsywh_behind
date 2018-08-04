package com.Entitys.FileUpload.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name="filestore")
public class FileStore extends Bean {
	@Column(name="filename", length=100)
	@Valid(varType = VarType.String, maxLength = 100)
	private String filename;
	@Column(name="filetype", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String filetype;
	@Column(name="time", length=13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long time;
	@Column(name="url")
	@Valid(needValid = false)
	private String url;
	@Column(name="savepath")
	@Valid(needValid = false)
	private String savepath;
	public FileStore() {
		super();
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getFiletype() {
		return filetype;
	}
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSavepath() {
		return savepath;
	}
	public void setSavepath(String savepath) {
		this.savepath = savepath;
	}
}
