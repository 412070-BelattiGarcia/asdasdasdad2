package ar.edu.utn.frc.tup.piii.dtos.matches;

import com.fasterxml.jackson.annotation.JsonAlias;

public class CreateMatchRequest {

    private String player1Id;
    private String player1Name;
    private String player1DeckId;
    private String player2Id;
    private String player2Name;
    private String player2DeckId;

    public String getPlayer1Id() {
        return player1Id;
    }

    @JsonAlias("playerId")
    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    @JsonAlias("playerName")
    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer1DeckId() {
        return player1DeckId;
    }

    @JsonAlias("deckId")
    public void setPlayer1DeckId(String player1DeckId) {
        this.player1DeckId = player1DeckId;
    }

    public String getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public String getPlayer2DeckId() {
        return player2DeckId;
    }

    public void setPlayer2DeckId(String player2DeckId) {
        this.player2DeckId = player2DeckId;
    }
}
