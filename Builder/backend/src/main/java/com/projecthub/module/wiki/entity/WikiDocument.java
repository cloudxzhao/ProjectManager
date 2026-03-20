package com.projecthub.module.wiki.entity;

import com.projecthub.module.wiki.enums.WikiStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Wiki 文档实体类 */
@Entity
@Table(name = "wiki_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE wiki_document SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WikiDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "parent_id")
  private Long parentId;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(name = "summary", length = 500)
  private String summary;

  @Column(name = "content_html", columnDefinition = "TEXT")
  private String contentHtml;

  @Column(name = "author_id", nullable = false)
  private Long authorId;

  @Column(nullable = false)
  @Builder.Default
  private Integer version = 1;

  @Column(name = "order_num")
  @Builder.Default
  private Integer orderNum = 0;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  @Builder.Default
  private WikiStatus status = WikiStatus.PUBLISHED;

  @Column(name = "parent_path", length = 1000)
  private String parentPath;

  @Column @Builder.Default private Integer level = 0;

  @Column(name = "view_count")
  @Builder.Default
  private Integer viewCount = 0;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
