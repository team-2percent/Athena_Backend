package goorm.athena.domain.search.entity;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.*;

@Entity
@RequiredArgsConstructor
@Table(name = "search")
public class Search {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "search_word")
  private String searchWord;

}
