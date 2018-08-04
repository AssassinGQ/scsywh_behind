package com.Entitys.Exam.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;

@Entity
@Table(name="exam")
public class Exam extends Bean {
	@Column(name="name_", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String name;
	@Column(name="desc_", length=30)
	@Valid(varType = VarType.String, maxLength = 30)
	private String desc;
	//jsonobject(题号：jsonobject)
//	{
//		questionid:string题目标识
//		question:string题目字符串
//		type:int题目类型
//		choices:选项，jsonobject(标号：选项))
//		answer:int,选项标号
//		score:double
//	}
	@Column(name="questions")
	@Valid(needValid = false)
	private String questions;
	public Exam() {
		super();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getQuestions() {
		return questions;
	}
	public void setQuestions(String questions) {
		this.questions = questions;
	}
	public Map<String, Question> getQuestionsMap(){
		Map<String, Question> ret = new HashMap<String, Question>();
		try {
			JSONArray questionsArray = new JSONArray(this.questions);
			for(int i = 0; i <questionsArray.length(); i++){
				JSONObject questionObject = questionsArray.getJSONObject(i);
				String key = questionObject.getString("questionid");
				ret.put(key, new Question(questionObject));
			}
		} catch (JSONException e) {
		}
		return ret;
	}
	public double getScore(JSONObject answerObject){
		Map<String, Question> questions = this.getQuestionsMap();
		double score_total = 0L;
		try {
			Iterator<Entry<String, Question>> iterator = questions.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<String, Question> entry = iterator.next();
				Question question = entry.getValue();
				double score_this = question.getScore();
				String answer_this = question.getAnswer();
				String answer_choose = answerObject.getString(String.valueOf(question.getQuestionid()));
				if(answer_choose.equals(answer_this))
					score_total += score_this;
			}
		} catch (JSONException e) {}
		return score_total;
	}
}
