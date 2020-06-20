package tools;

import java.util.HashMap;

public class MathEvaluator {

  protected static Operator[] operators = null;
  private Node node = null;
  private String expression = null;
  private HashMap<String, Double> variables = new HashMap<>();

  public MathEvaluator() {
    init();
  }

  public MathEvaluator(String s) {
    init();
    setExpression(s);
  }

  private void init() {
    if (operators == null) {
      initializeOperators();
    }
  }

  public void addVariable(String v, double val) {
    addVariable(v, new Double(val));
  }

  public void addVariable(String v, Double val) {
    variables.put(v, val);
  }

  private void setExpression(String s) {
    expression = s;
  }

  public Double getValue() {
    if (expression == null) {
      return null;
    }
    try {
      node = new Node(expression);
      return evaluate(node);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Double evaluate(Node n) {
    if (n.hasOperator() && n.hasChild()) {
      if (n.getOperator().getType() == 1) {
        n.setValue(evaluateExpression(n.getOperator(), evaluate(n.getLeft()), null));
      } else if (n.getOperator().getType() == 2) {
        n.setValue(evaluateExpression(n.getOperator(), evaluate(n.getLeft()), evaluate(n.getRight())));
      }
    }
    return n.getValue();
  }

  private static Double evaluateExpression(Operator o, Double f1, Double f2) {
    String op = o.getOperator();
    Double res = null;
    switch (op) {
      case "+":
        res = new Double(f1.doubleValue() + f2.doubleValue());
        break;
      case "-":
        res = new Double(f1.doubleValue() - f2.doubleValue());
        break;
      case "*":
        res = new Double(f1.doubleValue() * f2.doubleValue());
        break;
      case "/":
        res = new Double(f1.doubleValue() / f2.doubleValue());
        break;
      case "floor":
        res = new Double(Math.floor(f1.doubleValue()));
        break;
      case "ceil":
        res = new Double(Math.ceil(f1.doubleValue()));
        break;
    }

    return res;
  }

  private void initializeOperators() {
    operators = new Operator[6];
    operators[0] = new Operator("+", 2, 0);
    operators[1] = new Operator("-", 2, 0);
    operators[2] = new Operator("*", 2, 10);
    operators[3] = new Operator("/", 2, 10);
    operators[4] = new Operator("floor", 1, 20);
    operators[5] = new Operator("ceil", 1, 20);

  }

  public Double getVariable(String s) {
    return (Double) variables.get(s);
  }

  private Double getDouble(String s) {
    if (s == null) {
      return null;
    }

    Double res = null;
    try {
      res = new Double(Double.parseDouble(s));
    } catch (Exception e) {
      return getVariable(s);
    }

    return res;
  }

  protected Operator[] getOperators() {
    return operators;
  }

  protected class Operator {

    private String op;
    private int type;
    private int priority;

    public Operator(String o, int t, int p) {
      op = o;
      type = t;
      priority = p;
    }

    public String getOperator() {
      return op;
    }

    public void setOperator(String o) {
      op = o;
    }

    public int getType() {
      return type;
    }

    public int getPriority() {
      return priority;
    }
  }

  protected class Node {

    public String nString = null;
    public Operator nOperator = null;
    public Node nLeft = null;
    public Node nRight = null;
    public Node nParent = null;
    public int nLevel = 0;
    public Double nValue = null;

    public Node(String s) throws Exception {
      init(null, s, 0);
    }

    public Node(Node parent, String s, int level) throws Exception {
      init(parent, s, level);
    }

    private void init(Node parent, String s, int level) throws Exception {
      s = removeIllegalCharacters(s);
      s = removeBrackets(s);
      s = addZero(s);
      if (checkBrackets(s) != 0) {
        throw new Exception("Wrong number of brackets in [" + s + "]");
      }

      nParent = parent;
      nString = s;
      nValue = getDouble(s);
      nLevel = level;
      int sLength = s.length();
      int inBrackets = 0;
      int startOperator = 0;

      for (int i = 0; i < sLength; i++) {
        if (s.charAt(i) == '(') {
          inBrackets++;
        } else if (s.charAt(i) == ')') {
          inBrackets--;
        } else {
          // the expression must be at "root" level
          if (inBrackets == 0) {
            Operator o = getOperator(nString, i);
            if (o != null) {
              // if first operator or lower priority operator
              if (nOperator == null || nOperator.getPriority() >= o.getPriority()) {
                nOperator = o;
                startOperator = i;
              }
            }
          }
        }
      }

      if (nOperator != null) {
        // one operand, should always be at the beginning
        if (startOperator == 0 && nOperator.getType() == 1) {
          // the brackets must be ok
          if (checkBrackets(s.substring(nOperator.getOperator().length())) == 0) {
            nLeft = new Node(this, s.substring(nOperator.getOperator().length()), nLevel + 1);
            nRight = null;
            return;
          } else {
            throw new Exception("Error during parsing... missing brackets in [" + s + "]");
          }
        } // two operands
        else if (startOperator > 0 && nOperator.getType() == 2) {
          //nOperator = nOperator;
          nLeft = new Node(this, s.substring(0, startOperator), nLevel + 1);
          nRight = new Node(this, s.substring(startOperator + nOperator.getOperator().length()), nLevel + 1);
        }
      }
    }

    private Operator getOperator(String s, int start) {
      Operator[] operators = getOperators();
      String temp = s.substring(start);
      temp = getNextWord(temp);
      for (int i = 0; i < operators.length; i++) {
        if (temp.startsWith(operators[i].getOperator())) {
          return operators[i];
        }
      }
      return null;
    }

    private String getNextWord(String s) {
      int sLength = s.length();
      for (int i = 1; i < sLength; i++) {
        char c = s.charAt(i);
        if ((c > 'z' || c < 'a') && (c > '9' || c < '0')) {
          return s.substring(0, i);
        }
      }
      return s;
    }

    protected int checkBrackets(String s) {
      int sLength = s.length();
      int inBracket = 0;

      for (int i = 0; i < sLength; i++) {
        if (s.charAt(i) == '(' && inBracket >= 0) {
          inBracket++;
        } else if (s.charAt(i) == ')') {
          inBracket--;
        }
      }

      return inBracket;
    }

    protected String addZero(String s) {
      if (s.startsWith("+") || s.startsWith("-")) {
        int sLength = s.length();
        for (int i = 0; i < sLength; i++) {
          if (getOperator(s, i) != null) {
            return "0" + s;
          }
        }
      }

      return s;
    }

    public void trace() {
      String op = getOperator() == null ? " " : getOperator().getOperator();
      _D(op + " : " + getString());
      if (this.hasChild()) {
        if (hasLeft()) {
          getLeft().trace();
        }
        if (hasRight()) {
          getRight().trace();
        }
      }
    }

    protected boolean hasChild() {
      return (nLeft != null || nRight != null);
    }

    protected boolean hasOperator() {
      return (nOperator != null);
    }

    protected boolean hasLeft() {
      return (nLeft != null);
    }

    protected Node getLeft() {
      return nLeft;
    }

    protected boolean hasRight() {
      return (nRight != null);
    }

    protected Node getRight() {
      return nRight;
    }

    protected Operator getOperator() {
      return nOperator;
    }

    protected int getLevel() {
      return nLevel;
    }

    protected Double getValue() {
      return nValue;
    }

    protected void setValue(Double f) {
      nValue = f;
    }

    protected String getString() {
      return nString;
    }

    public String removeBrackets(String s) {
      String res = s;
      if (s.length() > 2 && res.startsWith("(") && res.endsWith(")") && checkBrackets(s.substring(1, s.length() - 1)) == 0) {
        res = res.substring(1, res.length() - 1);
      }
      if (!res.equals(s)) {
        return removeBrackets(res);
      }
      return res;
    }

    public String removeIllegalCharacters(String s) {
      char[] illegalCharacters = {' '};
      String res = s;

      for (int j = 0; j < illegalCharacters.length; j++) {
        int i = res.lastIndexOf(illegalCharacters[j], res.length());
        while (i != -1) {
          String temp = res;
          res = temp.substring(0, i);
          res += temp.substring(i + 1);
          i = res.lastIndexOf(illegalCharacters[j], s.length());
        }
      }
      return res;
    }

    protected void _D(String s) {
      String nbSpaces = "";
      for (int i = 0; i < nLevel; i++) {
        nbSpaces += "  ";
      }
      System.out.println(nbSpaces + "|" + s);
    }
  }

  protected static void _D(String s) {
    System.err.println(s);
  }
}
