package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 0;
  private final static Map<String, TokenType> keywords;

  Scanner(String source) {
    this.source = source;
  }

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }

  // the interface run(source) will be calling
  // scan through the whole source, outputs the whole list of lexemes
  List<Token> scanTokens() {
    while (!endOfSource()) {
      // scanner is at the beginning of a new lexeme
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line)); 
    return tokens;
  }

  private boolean endOfSource() {
    return current >= source.length();
  }

  // scan through each chars, store one lexeme into the list at a time
  private void scanToken() {
    char c = advance();
    switch (c) {
      // single char
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break; 

      // single/double char(s)
      case '!': addToken(match('=')? BANG_EQUAL : BANG); break;
      case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break; 
      case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
      case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;

      // comment or slash
      case '/':
        if (match('/')) handleCommentSingle();
        else if (match('*')) handleCommentMultiple();
        else addToken(SLASH);
        break;

      // ignores spaces
      case ' ':
      case '\r':
      case '\t': break;
      // handles newline. it's a good practice to handle line increment just here, rather than seperately in many places
      case '\n': line++; break;
   
      // string literals
      case '"': handleString(); break;

      // number literals and unknown chars
      default: 
        if (isDigit(c)) handleNumber();
        else if (isAlpha(c)) handleIdentifier();
        else Lox.error(line, "Unexpected character."); break;
    }
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String lexeme = source.substring(start, current);
    tokens.add(new Token(type, lexeme, literal, line)); 
  }

  private boolean match(char next) {
    if (endOfSource()) return false;
    if (source.charAt(current) != next) return false;

    current ++;
    return true;
  }

  // just peek, no moving current
  private char peek() {
    if (endOfSource()) return '\0';
    return source.charAt(current);
  }

  // rather than adding a param in peek(steps), this is clearer that we only allow two lookaheads
  private char secondPeek() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  private void handleCommentSingle() {
    // swipe all the way right until seeing newline
    while (peek() != '\n' && !endOfSource()) advance();
  }

  // experiment with nests
  private void handleCommentMultiple() {
    // number of nests. increment when found /*; decrement when found */
    int nest = 1;

    while (!endOfSource()) {
      if (peek() == '\n') line ++;
      else if (peek() == '/' && secondPeek() == '*') {
        advance();
        nest ++;
      }
      else if (peek() == '*' && secondPeek() == '/') {
          nest --;
          advance();
          if (nest == 0) break;
      }
      advance();
    }
    if (!endOfSource()) advance();
  }

  private void handleString() {
    // Lox supports multi-line strings: remember to increment line
    while (peek() != '"' && !endOfSource()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (endOfSource()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // consumes the right "
    advance();

    // trim the "" for literal value
    addToken(STRING, source.substring(start + 1, current - 1));
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNum(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || isDigit(c);
  }

  private void handleNumber() {
    while (isDigit(peek())) advance();

    if (peek() == '.') {
      // 123.456: keep swiping
      if (isDigit(secondPeek())) {
        // consumes the .
        advance();
        while (isDigit(peek())) advance();
      }
      // else: 123.sqrt: end of the number literal
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void handleIdentifier() {
    while (isAlphaNum((peek()))) advance();
    String lexeme = source.substring(start, current);

    TokenType type = keywords.get(lexeme);
    if (type == null) addToken(IDENTIFIER);
    else addToken(type);
  }
}
