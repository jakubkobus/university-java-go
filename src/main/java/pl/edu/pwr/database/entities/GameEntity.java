package pl.edu.pwr.database.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class GameEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @PastOrPresent
  private LocalDateTime date;

  @NotBlank
  private String playerBlackType;
  
  @NotBlank
  private String playerWhiteType;

  private String result;

  @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<MoveEntity> moves = new ArrayList<>();

  public GameEntity() {
    this.date = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setPlayerBlackType(String type) {
    this.playerBlackType = type;
  }

  public void setPlayerWhiteType(String type) {
    this.playerWhiteType = type;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public List<MoveEntity> getMoves() {
    return moves;
  }

  public LocalDateTime getDate() {
    return date;
  }
}