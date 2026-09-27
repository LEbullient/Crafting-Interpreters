/*
study notes:
start off by thinking about the contract- how would it be called/ where would ptr be pointing at/ what to return (write test cases)
to verify the feasibility of the contract, check whether it's consistent across methods, and whether we have enough information
think in code first. after finish implementing, you  will see the connection between code and grammar
*/

package lox;

import static lox.TokenType.*;

import java.util.List;


class Parser {
  private int ptr = 0;
  private final List<Token> tokens;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  Expr parse() {
    try {
      return parseExpr();
    } catch (RuntimeException e){
      return null;
    }
  }

  private Expr parseExpr() {
    Expr expr = parseEquality();

    return expr;
  }

  private Expr parseEquality() {
    Expr expr = parseComparison();

    while (match(EQUAL_EQUAL, BANG_EQUAL)) {
      ptr++;
      expr = new Expr.Binary(expr, tokens.get(ptr-1), parseComparison());
    }

    return expr;
  }

  private Expr parseComparison() {
    Expr expr = parseTerm();

    while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
      ptr++;
      expr = new Expr.Binary(expr, tokens.get(ptr-1), parseTerm());
    }

    return expr;
  }

  private Expr parseTerm() {
    Expr expr = parseFactor();

    while (match(PLUS, MINUS)) {
      ptr++;
      expr = new Expr.Binary(expr, tokens.get(ptr-1), parseFactor());
    }

    return expr;
  }

  private Expr parseFactor() {
    Expr expr = parseSingle();

    while (match(STAR, SLASH)) {
      ptr++;
      expr = new Expr.Binary(expr, tokens.get(ptr-1), parseSingle());
    }

    return expr;
  }

  private Expr parseSingle() {
    Expr expr;
    if (match(LEFT_PAREN)) {
      expr = parseParen();
    } else if (match(BANG, MINUS)) {
      expr = parseUnary();
    } else if (match(STRING, NUMBER, TRUE, FALSE, IDENTIFIER)) {
      expr = parseLiteral();
    } else {
      throw new RuntimeException("expect a single");
    }

    return expr;
  }

  private Expr parseParen() {
    ptr++;
    Expr expr = parseExpr();

    if (match(RIGHT_PAREN)) {
      ptr++;
    } else {
      throw new RuntimeException("expect ')'");
    }

    return expr;
  }

  private Expr parseUnary() {
    ptr++;
    Expr expr = new Expr.Unary(tokens.get(ptr-1), parseSingle());
    return expr;
  }

  private Expr parseLiteral() {
    Expr expr = new Expr.Literal(tokens.get(ptr).literal);
    ptr++;
    return expr;
  }
 
  private boolean match(TokenType... tokenTypes) {
    for (TokenType tokenType : tokenTypes) {
      if (tokens.get(ptr).type == tokenType) {
        return true;
      }
    }
    return false;
  }
}