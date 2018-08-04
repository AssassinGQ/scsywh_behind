package com.Entitys.Exam.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.Common.Annitations.Valid;
import com.Common.Annitations.Valid.VarType;
import com.Common.Entitys.Bean;
import com.Common.Utils.Utils;

@Entity
@Table(name="scorehistory")
public class ScoreHistory extends Bean {
	@Column(name="user_sid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long user_sid;
	@Column(name="exam_sid", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Long exam_sid;
	@Column(name="time_", length=13)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999999D)
	private Long time;
	@Column(name="score_", length=10)
	@Valid(varType = VarType.Number, minValue = 0, maxValue = 9999999999D)
	private Double score;
	public ScoreHistory() {
		super();
	}
	public ScoreHistory(Long user_sid, Long exam_sid, Double score) {
		super();
		this.user_sid = user_sid;
		this.exam_sid = exam_sid;
		this.score = score;
		this.time = Utils.getCurrenttimeMills();
	}
	public Long getUser_sid() {
		return user_sid;
	}
	public void setUser_sid(Long user_sid) {
		this.user_sid = user_sid;
	}
	public Long getExam_sid() {
		return exam_sid;
	}
	public void setExam_sid(Long exam_sid) {
		this.exam_sid = exam_sid;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
}
