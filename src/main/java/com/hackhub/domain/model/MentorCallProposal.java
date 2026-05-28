package com.hackhub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_call_proposals")
public class MentorCallProposal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "support_request_id", nullable = false)
	private SupportRequest supportRequest;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mentor_id", nullable = false)
	private User mentor;

	@Column(nullable = false)
	private LocalDateTime scheduledAt;

	@Column(nullable = false)
	private String externalCallId;

	@Column(nullable = false)
	private String bookingUrl;

	@Column(nullable = false)
	private LocalDateTime createdAt;
}
