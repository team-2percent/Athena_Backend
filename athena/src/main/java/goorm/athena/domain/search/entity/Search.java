package goorm.athena.domain.search.entity;

import jakarta.persistence.*;

public class Search {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "search_word")
  private String searchWord;

}
