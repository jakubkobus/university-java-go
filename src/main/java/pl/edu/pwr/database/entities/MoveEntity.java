package pl.edu.pwr.database.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "moves")
public class MoveEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Min(1)
  private int moveNumber;
  
  @Min(0)
  @Max(18)
  private int x;
  
  @Min(0)
  @Max(18)
  private int y;

  @NotBlank
  private String color;

  @NotBlank
  private String type;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "game_id")
  private GameEntity game;

  public MoveEntity() {
  }

  public MoveEntity(GameEntity game, int moveNumber, int x, int y, String color, String type) {
    this.game = game;
    this.moveNumber = moveNumber;
    this.x = x;
    this.y = y;
    this.color = color;
    this.type = type;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public String getColor() {
    return color;
  }

  public String getType() {
    return type;
  }
}