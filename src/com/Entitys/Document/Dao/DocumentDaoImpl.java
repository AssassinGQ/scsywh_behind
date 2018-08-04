package com.Entitys.Document.Dao;

import org.hibernate.Session;

import com.Common.Daos.SingTabBaseDaoImpl;
import com.Entitys.Document.Entity.Document;

public class DocumentDaoImpl extends SingTabBaseDaoImpl<Document> implements DocumentDao {

	public DocumentDaoImpl(Session session) {
		super(session, Document.class);
		this.pretname = "document_";
		this.tAliasname = "document";
	}	
}
