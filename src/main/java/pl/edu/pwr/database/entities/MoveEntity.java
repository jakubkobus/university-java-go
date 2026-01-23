package pl.edu.pwr.database.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "moves")
public class MoveEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private int moveNumber;
  private int x;
  private int y;

  private String color;

  private String type;

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