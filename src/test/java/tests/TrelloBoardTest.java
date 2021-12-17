package tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.not;
import static constants.ResponseStatus.GOOD_RESPONSE;
import static constants.ResponseStatus.BAD_RESPONSE;
import static constants.ResponseStatus.NOT_FOUND_RESPONSE;

import beans.TrelloBoard;
import constants.BoardParameters;
import core.TrelloServiceObj;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TrelloBoardTest {
    public final int NAME_LENGTH = 16;
    public final int ID_LENGTH = 24;
    TrelloBoard board;

    @BeforeMethod(onlyForGroups = {"setUp"})
    public void createBoard() {
        board = TrelloServiceObj.createBoard(
            RandomStringUtils.randomAlphabetic(NAME_LENGTH), GOOD_RESPONSE
        );
    }

    @AfterMethod(onlyForGroups = {"tearDown"})
    public void deleteBoard() {
        TrelloServiceObj.deleteBoard(board.getId(), GOOD_RESPONSE);
    }

    @Test(groups = {"setUp", "tearDown"})
    public void createNewBoardTest() {
        assertThat(board.getClosed(), equalTo(false));
        assertThat(board, is(not(nullValue())));
    }

    @Test(groups = {"setUp", "tearDown"})
    public void updateDescriptionTest() {
        String description = RandomStringUtils.randomAlphabetic(NAME_LENGTH);
        board = TrelloServiceObj.updateBoard(
            board.getId(), BoardParameters.DESCRIPTION.getParameterName(), description,
            GOOD_RESPONSE
        );
        assertThat(board.getDesc(), containsString(description));
    }

    @Test
    public void createBoardsWithSameNamesTest() {
        String name = RandomStringUtils.randomAlphabetic(NAME_LENGTH);
        TrelloBoard board1 = TrelloServiceObj.createBoard(name, GOOD_RESPONSE);
        TrelloBoard board2 = TrelloServiceObj.createBoard(name, GOOD_RESPONSE);

        assertThat(board1.equals(board2), equalTo(false));
        TrelloServiceObj.deleteBoard(board1.getId(), GOOD_RESPONSE);
        TrelloServiceObj.deleteBoard(board2.getId(), GOOD_RESPONSE);
    }

    @Test(groups = {"setUp"})
    public void deleteBoardTest() {
        TrelloServiceObj.deleteBoard(board.getId(), GOOD_RESPONSE);
        TrelloServiceObj.getBoard(board.getId(), NOT_FOUND_RESPONSE);
    }

    @Test(groups = {"setUp", "tearDown"})
    public void closeBoardTest() {
        board = TrelloServiceObj.updateBoard(
            board.getId(), BoardParameters.CLOSED.getParameterName(), "true",
            GOOD_RESPONSE
        );
        assertThat(board.getClosed(), equalTo(true));
    }

    @Test
    public void closeNonExistentBoardTest() {
        TrelloBoard board = TrelloServiceObj.updateBoard(
            RandomStringUtils.randomAlphabetic(ID_LENGTH),
            BoardParameters.CLOSED.getParameterName(), "true",
            BAD_RESPONSE
        );
    }

    @Test
    public void deleteNonExistentBoardTest() {
        TrelloBoard board = TrelloServiceObj.getBoard(
            RandomStringUtils.randomAlphabetic(ID_LENGTH),
            BAD_RESPONSE
        );
    }

    @Test
    public void getNonExistentBoardTest() {
        TrelloBoard board = TrelloServiceObj.getBoard(
            RandomStringUtils.randomAlphabetic(ID_LENGTH),
            BAD_RESPONSE
        );
        assertThat(board, is(nullValue()));
    }
}
