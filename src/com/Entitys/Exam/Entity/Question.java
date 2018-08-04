package com.Entitys.Exam.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Question {
	public static final int TYPE_SINGLE = 0;
	public static final int TYPE_MULTI = 1;
	private String questionid;
	private String question;
	private Integer type;
	private Map<String, String> choices;
	private String answer;//表示哪个标号的选项是对的
	private Double score;
	public Question() {
		super();
	}
	public Question(JSONObject jsonObject) {
		super();			
		try {
			this.questionid = jsonObject.getString("questionid");
			this.question = jsonObject.getString("question");
			this.type = jsonObject.getInt("type");
			if(this.type != TYPE_SINGLE && this.type != TYPE_MULTI)
				this.type = TYPE_SINGLE;
			this.answer = jsonObject.getString("answer");
			this.score = jsonObject.getDouble("score");
			JSONObject choicesObject = jsonObject.getJSONObject("choices");
			choices = new HashMap<String, String>();
			@SuppressWarnings("unchecked")
			Iterator<String> iterator = choicesObject.keys();
			while(iterator.hasNext()){
				String key = iterator.next();
				String value = choicesObject.getString(key);
				choices.put(key, value);
			}
		} catch (Exception e) {
			return;
		}
	}
	public String getQuestionid() {
		return questionid;
	}
	public void setQuestionid(String questionid) {
		this.questionid = questionid;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Map<String, String> getChoices() {
		return choices;
	}
	public void setChoices(Map<String, String> choices) {
		this.choices = choices;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public void addChoice(String key, String choice){
		if(choices == null)
			choices = new HashMap<String, String>();
		choices.put(key, choice);
	}
	public JSONObject getChoicesObject(){
		if(choices == null)
			return new JSONObject();
		else
			return new JSONObject(choices);
	}
	public JSONObject getQuestionObject(){
		JSONObject retObject = new JSONObject();
		try {
			retObject.put("question", this.question);
			retObject.put("type", this.type);
			retObject.put("choices", this.getChoicesObject());
			retObject.put("answer", this.answer);
			retObject.put("score", this.score);
			return retObject;
		} catch (JSONException e) {
			retObject = new JSONObject();
			return retObject;
		}
	}
}
