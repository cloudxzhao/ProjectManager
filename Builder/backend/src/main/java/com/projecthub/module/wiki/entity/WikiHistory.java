package com.projecthub.module.wiki.entity;

import com.projecthub.module.wiki.enums.WikiChangeType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Wiki 历史记录实体类 */
@Entity
@Table(name = "wiki_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WikiHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "document_id", nullable = false)
  private Long documentId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  @Builder.Default
  private Integer version = 1;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "content_html", columnDefinition = "TEXT")
  private String contentHtml;

  @Column(name = "change_log", length = 500)
  private String changeLog;

  @Enumerated(EnumType.STRING)
  @Column(name = "change_type", length = 20)
  @Builder.Default
  private WikiChangeType changeType = WikiChangeType.UPDATE;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
