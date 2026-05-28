package com.hackhub.application.service;

import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class StaffAccessService {

	public boolean isOrganizerOf(User user, Hackathon hackathon) {
		return user != null &&
			hackathon != null &&
			hackathon.getOrganizer() != null &&
			hackathon.getOrganizer().getId().equals(user.getId());
	}

	public boolean isJudgeOf(User user, Hackathon hackathon) {
		return user != null &&
			hackathon != null &&
			user.getRole() == Role.JUDGE &&
			hackathon.getJudges().stream().anyMatch(judge -> judge.getId().equals(user.getId()));
	}

	public boolean isMentorOf(User user, Hackathon hackathon) {
		return user != null &&
			hackathon != null &&
			user.getRole() == Role.MENTOR &&
			hackathon.getMentors().stream().anyMatch(mentor -> mentor.getId().equals(user.getId()));
	}

	public boolean canAccessSubmissions(User user, Hackathon hackathon) {
		return isOrganizerOf(user, hackathon) ||
			isJudgeOf(user, hackathon) ||
			isMentorOf(user, hackathon);
	}
}
