package org.jessies.calc;

/*
 * This file is part of LittleHelper.
 * Copyright (C) 2009 Elliott Hughes <enh@jessies.org>.
 * 
 * LittleHelper is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.math.*;
import java.util.*;
import org.jessies.test.*;

// FIXME: Mac OS' calculator offers -d variants of all the trig functions for degrees. that, or offer constants to multiply by to convert to degrees/radians?
// FIXME: higher-order built-in functions like http://www.vitanuova.com/inferno/man/1/calc.html (sum, product, integral, differential, solve).
// FIXME: integer division (//).
public class Calculator {
    private final Map<String, CalculatorFunction> functions;
    private final Map<CalculatorToken, CalculatorFunction> operators;
    private final Map<String, Variable> variables;
    private final Variable ans;
    private CalculatorPlotter plotter;
    
    // Variable names are case-insensitive but case-preserving.
    // We implement case-insensitivity by using name.toLowerCase() as the key.
    // We preserve case by using a pair of the first-encountered name and the variable's value as the map's value type.
    static class Variable {
        String name;
        Node value;
        boolean isAssignable = true;
    }
    
    public Calculator() {
        this.functions = new HashMap<String, CalculatorFunction>();
        this.operators = new EnumMap<CalculatorToken, CalculatorFunction>(CalculatorToken.class);
        this.variables = new HashMap<String, Variable>();
        this.ans = initAns();
        initBuiltInConstants();
        initBuiltInFunctions();
    }
    
    public void setPlotter(CalculatorPlotter plotter) {
        this.plotter = plotter;
    }
    
    public CalculatorPlotter getPlotter() {
        return plotter;
    }
    
    private Variable initAns() {
        final Variable result = new Variable();
        result.name = "Ans";
        result.value = null;
        result.isAssignable = false;
        variables.put(result.name.toLowerCase(), result);
        return result;
    }
    
    private void initBuiltInConstants() {
        // FIXME: use higher-precision string forms?
        initConstant("e", new RealNode(Math.E));
        
        final Node pi = new RealNode(Math.PI);
        initConstant("pi", pi);
        initConstant("\u03c0", pi);
        
        initConstant("false", BooleanNode.FALSE);
        initConstant("true", BooleanNode.TRUE);
    }
    
    private void initConstant(String name, Node value) {
        final Variable constant = new Variable();
        constant.name = name;
        constant.value = value;
        constant.isAssignable = false;
        final String key = name.toLowerCase();
        variables.put(key, constant);
    }
    
    private void initBuiltInFunctions() {
        // FIXME: acosh, asinh, atanh, chop, clip.
        addFunction(new CalculatorFunctions.Abs(),            "Abs");
        addFunction(new CalculatorFunctions.Acos(),           "Acos", "ArcCos");
        addFunction(new CalculatorFunctions.And(),            "And");
        addFunction(new CalculatorFunctions.Asin(),           "Asin", "ArcSin");
        addFunction(new CalculatorFunctions.Atan2(),          "Atan2");
        addFunction(new CalculatorFunctions.Atan(),           "Atan", "ArcTan");
        addFunction(new CalculatorFunctions.BitAnd(),         "BitAnd");
        addFunction(new CalculatorFunctions.BitNot(),         "BitNot");
        addFunction(new CalculatorFunctions.BitOr(),          "BitOr");
        addFunction(new CalculatorFunctions.BitShiftRight() , "BitShiftRight");
        addFunction(new CalculatorFunctions.BitShiftLeft(),   "BitShiftLeft");
        addFunction(new CalculatorFunctions.BitXor(),         "BitXor");
        addFunction(new CalculatorFunctions.Cbrt(),           "Cbrt");
        addFunction(new CalculatorFunctions.Ceiling(),        "Ceiling", "Ceil");
        addFunction(new CalculatorFunctions.Cosh(),           "Cosh");
        addFunction(new CalculatorFunctions.Cos(),            "Cos");
        addFunction(new CalculatorFunctions.Define(),         "Define");
        addFunction(new CalculatorFunctions.Divide(),         "Divide");
        addFunction(new CalculatorFunctions.Equal(),          "Equal");
        addFunction(new CalculatorFunctions.Exp(),            "Exp");
        addFunction(new CalculatorFunctions.Factorial(),      "Factorial");
        addFunction(new CalculatorFunctions.Floor(),          "Floor");
        addFunction(new CalculatorFunctions.FractionalPart(), "FractionalPart");
        addFunction(new CalculatorFunctions.GreaterEqual(),   "GreaterEqual");
        addFunction(new CalculatorFunctions.Greater(),        "Greater");
        addFunction(new CalculatorFunctions.Hypot(),          "Hypot");
        addFunction(new CalculatorFunctions.IntegerLength(),  "IntegerLength");
        addFunction(new CalculatorFunctions.IntegerPart(),    "IntegerPart");
        addFunction(new CalculatorFunctions.IsPrime(),        "IsPrime");
        addFunction(new CalculatorFunctions.LessEqual(),      "LessEqual");
        addFunction(new CalculatorFunctions.Less(),           "Less");
        addFunction(new CalculatorFunctions.Log10(),          "Log10");
        addFunction(new CalculatorFunctions.Log2(),           "Log2");
        addFunction(new CalculatorFunctions.LogE(),           "LogE", "Ln");
        addFunction(new CalculatorFunctions.Log(),            "Log");
        addFunction(new CalculatorFunctions.Max(),            "Max");
        addFunction(new CalculatorFunctions.Min(),            "Min");
        addFunction(new CalculatorFunctions.Mod(),            "Mod");
        addFunction(new CalculatorFunctions.Not(),            "Not");
        addFunction(new CalculatorFunctions.Or(),             "Or");
        addFunction(new CalculatorFunctions.Plot(),           "Plot");
        addFunction(new CalculatorFunctions.Plus(),           "Plus");
        addFunction(new CalculatorFunctions.Power(),          "Power");
        addFunction(new CalculatorFunctions.Product(),        "Product", /* Unicode Greek capital letter pi */ "\u03a0", /* Unicode product sign */ "\u220f");
        addFunction(new CalculatorFunctions.Random(),         "Random", "rand");
        addFunction(new CalculatorFunctions.Round(),          "Round");
        addFunction(new CalculatorFunctions.Sign(),           "Sign");
        addFunction(new CalculatorFunctions.Sinh(),           "Sinh");
        addFunction(new CalculatorFunctions.Sin(),            "Sin");
        addFunction(new CalculatorFunctions.Sqrt(),           "Sqrt");
        addFunction(new CalculatorFunctions.Sum(),            "Sum", /* Unicode Greek capital letter sigma */ "\u03a3", /* Unicode summation sign */ "\u2211");
        addFunction(new CalculatorFunctions.Subtract(),       "Subtract");
        addFunction(new CalculatorFunctions.Tanh(),           "Tanh");
        addFunction(new CalculatorFunctions.Tan(),            "Tan");
        addFunction(new CalculatorFunctions.Times(),          "Times");
        addFunction(new CalculatorFunctions.Unequal(),        "Unequal");
        
        operators.put(CalculatorToken.B_AND, functions.get("BitAnd"));
        operators.put(CalculatorToken.B_NOT, functions.get("BitNot"));
        operators.put(CalculatorToken.B_OR,  functions.get("BitOr"));
        operators.put(CalculatorToken.DIV,   functions.get("Divide"));
        operators.put(CalculatorToken.EQ,    functions.get("Equal"));
        operators.put(CalculatorToken.GE,    functions.get("GreaterEqual"));
        operators.put(CalculatorToken.GT,    functions.get("Greater"));
        operators.put(CalculatorToken.L_AND, functions.get("And"));
        operators.put(CalculatorToken.LE,    functions.get("LessEqual"));
        operators.put(CalculatorToken.L_OR,  functions.get("Or"));
        operators.put(CalculatorToken.LT,    functions.get("Less"));
        operators.put(CalculatorToken.MINUS, functions.get("Subtract"));
        operators.put(CalculatorToken.MOD,   functions.get("Mod"));
        operators.put(CalculatorToken.MUL,   functions.get("Times"));
        operators.put(CalculatorToken.NE,    functions.get("Unequal"));
        operators.put(CalculatorToken.PLUS,  functions.get("Plus"));
        operators.put(CalculatorToken.POW,   functions.get("Power"));
        operators.put(CalculatorToken.SHL,   functions.get("BitShiftLeft"));
        operators.put(CalculatorToken.SHR,   functions.get("BitShiftRight"));
    }
    
    private void addFunction(CalculatorFunction function, String... names) {
        for (String name : names) {
            addUniqueFunction(function, name);
            final String lowerCaseName = name.toLowerCase();
            if (!lowerCaseName.equals(name)) {
                addUniqueFunction(function, lowerCaseName);
            }
            final String cStyleName = name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
            if (!cStyleName.equals(lowerCaseName)) {
                addUniqueFunction(function, cStyleName);
            }
        }
    }
    
    private void addUniqueFunction(CalculatorFunction function, String name) {
        // The checks in addFunction ensure no duplication for a single function.
        // This check is to avoid accidental duplication between functions.
        if (functions.get(name) != null) {
            throw new RuntimeException("function '" + name + "' already added");
        }
        functions.put(name, function);
    }
    
    public String evaluate(String stringExpression) throws CalculatorError {
        final CalculatorParser parser = new CalculatorParser(this, stringExpression);
        final Node expression = parser.parse();
        //System.err.println(tree);
        final Node result = expression.evaluate(this);
        ans.value = result;
        return result.toString();
    }
    
    public CalculatorFunction getFunction(String name) {
        return functions.get(name);
    }
    
    public CalculatorFunction getFunction(CalculatorToken token) {
        return operators.get(token);
    }
    
    public Node getVariable(String name) {
        final Variable v = variables.get(name.toLowerCase());
        return (v != null) ? v.value : null;
    }
    
    public void setVariable(String name, Node newValue) {
        final String key = name.toLowerCase();
        Variable v = variables.get(key);
        if (v == null) {
            v = new Variable();
            v.name = name;
            variables.put(key, v);
        } else  if (!v.isAssignable) {
            if (v.name.equals("Ans")) {
                throw new CalculatorError("can't assign a new value to Ans");
            } else {
                throw new CalculatorError("can't assign a new value to the constant " + v.name);
            }
        }
        v.value = newValue;
    }
    
    @Test private static void testArithmetic() {
        Assert.equals(new Calculator().evaluate("0"), "0");
        Assert.equals(new Calculator().evaluate("1"), "1");
        Assert.equals(new Calculator().evaluate("-1"), "-1");
        Assert.equals(new Calculator().evaluate("--1"), "1");
        Assert.equals(new Calculator().evaluate("1.00"), "1.0");
        
        Assert.equals(new Calculator().evaluate(".2"), "0.2");
        
        Assert.equals(new Calculator().evaluate("1.2E3"), "1200.0");
        Assert.equals(new Calculator().evaluate("1E3"), "1000");
        Assert.equals(new Calculator().evaluate("1E-3"), "0.0010");
        Assert.equals(new Calculator().evaluate("1.E3"), "1000.0");
        Assert.equals(new Calculator().evaluate(".1E3"), "100.0");
        
        Assert.equals(new Calculator().evaluate("1.2E3"), new Calculator().evaluate("1.2e3"));
        
        Assert.equals(new Calculator().evaluate("1+2+3"), "6");
        Assert.equals(new Calculator().evaluate("1+-2"), "-1");
        Assert.equals(new Calculator().evaluate("3-2-1"), "0");
        Assert.equals(new Calculator().evaluate("10000+0.001"), "10000.001");
        Assert.equals(new Calculator().evaluate("0.001+10000"), "10000.001");
        Assert.equals(new Calculator().evaluate("10000-0.001"), "9999.999");
        Assert.equals(new Calculator().evaluate("0.001-10000"), "-9999.999");
        
        Assert.equals(new Calculator().evaluate("3*4"), "12");
        Assert.equals(new Calculator().evaluate("-3*4"), "-12");
        Assert.equals(new Calculator().evaluate("3*-4"), "-12");
        Assert.equals(new Calculator().evaluate("-3*-4"), "12");
        
        Assert.equals(new Calculator().evaluate("1+2*3"), "7");
        Assert.equals(new Calculator().evaluate("(1+2)*3"), "9");
        
        Assert.equals(new Calculator().evaluate("1/2"), "0.5");
        
        Assert.equals(new Calculator().evaluate("3%4"), "3");
        Assert.equals(new Calculator().evaluate("4%4"), "0");
        Assert.equals(new Calculator().evaluate("5%4"), "1");
    }
    
    @Test private static void testRelationalOperations() {
        Assert.equals(new Calculator().evaluate("1<2"), "true");
        Assert.equals(new Calculator().evaluate("2<2"), "false");
        Assert.equals(new Calculator().evaluate("2<1"), "false");
        Assert.equals(new Calculator().evaluate("1<=2"), "true");
        Assert.equals(new Calculator().evaluate("2<=2"), "true");
        Assert.equals(new Calculator().evaluate("2<=1"), "false");
        Assert.equals(new Calculator().evaluate("1>2"), "false");
        Assert.equals(new Calculator().evaluate("2>2"), "false");
        Assert.equals(new Calculator().evaluate("2>1"), "true");
        Assert.equals(new Calculator().evaluate("1>=2"), "false");
        Assert.equals(new Calculator().evaluate("2>=2"), "true");
        Assert.equals(new Calculator().evaluate("2>=1"), "true");
        Assert.equals(new Calculator().evaluate("1==2"), "false");
        Assert.equals(new Calculator().evaluate("2==2"), "true");
        Assert.equals(new Calculator().evaluate("2==1"), "false");
        Assert.equals(new Calculator().evaluate("1!=2"), "true");
        Assert.equals(new Calculator().evaluate("2!=2"), "false");
        Assert.equals(new Calculator().evaluate("2!=1"), "true");
        Assert.equals(new Calculator().evaluate("true==true"), "true");
        Assert.equals(new Calculator().evaluate("true==false"), "false");
        Assert.equals(new Calculator().evaluate("false==true"), "false");
        Assert.equals(new Calculator().evaluate("false==false"), "true");
        Assert.equals(new Calculator().evaluate("true!=true"), "false");
        Assert.equals(new Calculator().evaluate("true!=false"), "true");
        Assert.equals(new Calculator().evaluate("false!=true"), "true");
        Assert.equals(new Calculator().evaluate("false!=false"), "false");
    }
    
    @Test private static void testLogicalNot() {
        Assert.equals(new Calculator().evaluate("!false"), "true");
        Assert.equals(new Calculator().evaluate("!true"), "false");
        Assert.equals(new Calculator().evaluate("!(1==2)"), "true");
        Assert.equals(new Calculator().evaluate("!(2==2)"), "false");
        Assert.equals(new Calculator().evaluate("!!(2==2)"), "true");
    }
    
    @Test private static void testLogicalAnd() {
        Assert.equals(new Calculator().evaluate("false&&false"), "false");
        Assert.equals(new Calculator().evaluate("false&&true"), "false");
        Assert.equals(new Calculator().evaluate("true&&false"), "false");
        Assert.equals(new Calculator().evaluate("true&&true"), "true");
    }
    
    @Test private static void testLogicalOr() {
        Assert.equals(new Calculator().evaluate("false||false"), "false");
        Assert.equals(new Calculator().evaluate("false||true"), "true");
        Assert.equals(new Calculator().evaluate("true||false"), "true");
        Assert.equals(new Calculator().evaluate("true||true"), "true");
    }
    
    @Test private static void testShifts() {
        Assert.equals(new Calculator().evaluate("1<<4"), "16");
        Assert.equals(new Calculator().evaluate("(12<<3)>>3"), "12");
    }
    
    @Test private static void testBitOperations() {
        Assert.equals(new Calculator().evaluate("(0x1234 & 0xff0) == 0x230"), "true");
        Assert.equals(new Calculator().evaluate("(0x1200 | 0x34) == 0x1234"), "true");
        Assert.equals(new Calculator().evaluate("BitXor(5, 3)"), "6");
        Assert.equals(new Calculator().evaluate("((0x1234 & ~0xff) | 0x56) == 0x1256"), "true");
        Assert.equals(new Calculator().evaluate("~3"), "-4");
        Assert.equals(new Calculator().evaluate("~~3"), "3");
    }
    
    @Test private static void testExponentiation() {
        Assert.equals(new Calculator().evaluate("2^3"), "8");
        Assert.equals(new Calculator().evaluate("2^3^4"), "2417851639229258349412352");
        Assert.equals(new Calculator().evaluate("4^0.5"), "2.0");
        Assert.equals(new Calculator().evaluate("-10^2"), "100");
        Assert.equals(new Calculator().evaluate("(-10)^2"), "100");
        Assert.equals(new Calculator().evaluate("10^-2"), "0.01");
        Assert.equals(new Calculator().evaluate("10^(-2)"), "0.01");
    }
    
    @Test private static void testConstants() {
        Assert.equals(Double.valueOf(new Calculator().evaluate("e")), Math.E, 0.000001);
        Assert.equals(Double.valueOf(new Calculator().evaluate("pi")), Math.PI, 0.000001);
        Assert.equals(new Calculator().evaluate("pi == \u03c0"), "true");
        try {
            new Calculator().evaluate("pi = 3");
            Assert.failure("no exception was thrown when assigning to a constant!");
        } catch (CalculatorError ex) {
            Assert.equals(ex.getMessage(), "can't assign a new value to the constant pi");
        }
    }
    
    @Test private static void testBigIntegers() {
        // Arithmetic tests (from http://www.isthe.com/chongo/tech/comp/calc/calc-whatis.html).
        Assert.equals(new Calculator().evaluate("3 * 19^43 - 1"), "29075426613099201338473141505176993450849249622191102976");
        Assert.equals(new Calculator().evaluate("Mod(2^23209-1, 2^127-1)"), "39614081257132168796771975167");
        
        // Check switches from fix to big.
        Assert.equals(new Calculator().evaluate("Abs(-(0x8000)) == 0x8000"), "true");
        Assert.equals(new Calculator().evaluate("Abs(-(0x8000000000000000)) == 0x8000000000000000"), "true");
        Assert.equals(new Calculator().evaluate("-(0x8000000000000000) == -0x8000000000000000"), "true");
        Assert.equals(new Calculator().evaluate("-1 * 0x8000000000000000 == -0x8000000000000000"), "true");
        Assert.equals(new Calculator().evaluate("0x7fffffffffffffff + 1 == 0x8000000000000000"), "true");
        Assert.equals(new Calculator().evaluate("(-(0x8000000000000000)) - 1 == -0x8000000000000001"), "true");
        Assert.equals(new Calculator().evaluate("-1 * 0x8000000000000000 - 1 == -0x8000000000000001"), "true");
        Assert.equals(new Calculator().evaluate("-(-(0x8000000000000000)) == 0x8000000000000000"), "true");
        Assert.equals(new Calculator().evaluate("-1 * -1 * 0x8000000000000000 == 0x8000000000000000"), "true");
        Assert.equals(new Calculator().evaluate("0x8000000000000000/-1 == -0x8000000000000000"), "true");
    }
    
    @Test private static void testFunctions() {
        // FIXME: better tests?
        Assert.equals(new Calculator().evaluate("Abs(2)"), "2");
        Assert.equals(new Calculator().evaluate("Abs(-2)"), "2");
        Assert.equals(new Calculator().evaluate("Acos(1)"), "0.0");
        Assert.equals(new Calculator().evaluate("Asin(0)"), "0.0");
        Assert.equals(new Calculator().evaluate("Acos(0) == Asin(1)"), "true");
        Assert.equals(new Calculator().evaluate("Atan(0)"), "0.0");
        Assert.equals(new Calculator().evaluate("Cbrt(27)"), "3.0");
        Assert.equals(new Calculator().evaluate("Ceil(1.2)"), "2.0");
        Assert.equals(new Calculator().evaluate("Cos(0)"), "1.0");
        Assert.equals(new Calculator().evaluate("Cos(pi)"), "-1.0");
        Assert.equals(new Calculator().evaluate("Cosh(0)"), "1.0");
        Assert.equals(Double.valueOf(new Calculator().evaluate("Exp(1)/e")), 1.0, 0.000001);
        Assert.equals(new Calculator().evaluate("Floor(1.2)"), "1.0");
        Assert.equals(new Calculator().evaluate("Hypot(3, 4)"), "5.0");
        
        Assert.equals(new Calculator().evaluate("IsPrime(0)"), "false");
        Assert.equals(new Calculator().evaluate("IsPrime(1)"), "false");
        Assert.equals(new Calculator().evaluate("IsPrime(2)"), "true");
        Assert.equals(new Calculator().evaluate("IsPrime(3)"), "true");
        Assert.equals(new Calculator().evaluate("IsPrime(4)"), "false");
        Assert.equals(new Calculator().evaluate("IsPrime(5)"), "true");
        Assert.equals(new Calculator().evaluate("IsPrime(-4)"), "false");
        Assert.equals(new Calculator().evaluate("IsPrime(-5)"), "true");
        
        Assert.equals(new Calculator().evaluate("Log(2, 1024)"), "10.0");
        Assert.equals(new Calculator().evaluate("Log2(1024)"), "10.0");
        Assert.equals(new Calculator().evaluate("LogE(exp(4))"), "4.0");
        Assert.equals(new Calculator().evaluate("Log10(1000)"), "3.0");
        Assert.equals(new Calculator().evaluate("Round(1.2)"), "1");
        Assert.equals(new Calculator().evaluate("Round(1.8)"), "2");
        Assert.equals(new Calculator().evaluate("Sin(0)"), "0.0");
        Assert.equals(new Calculator().evaluate("Sin(pi/2)"), "1.0");
        Assert.equals(new Calculator().evaluate("Sinh(0)"), "0.0");
        Assert.equals(new Calculator().evaluate("Sqrt(81)"), "9.0");
        Assert.equals(new Calculator().evaluate("Tan(0)"), "0.0");
        Assert.equals(new Calculator().evaluate("Tanh(0)"), "0.0");
    }
    
    @Test private static void testFactorial() {
        Assert.equals(new Calculator().evaluate("Factorial(0)"), "1");
        Assert.equals(new Calculator().evaluate("Factorial(1)"), "1");
        Assert.equals(new Calculator().evaluate("Factorial(2)"), "2");
        Assert.equals(new Calculator().evaluate("Factorial(3)"), "6");
        Assert.equals(new Calculator().evaluate("Factorial(4)"), "24");
        Assert.equals(new Calculator().evaluate("Factorial(5)"), "120");
        
        Assert.equals(new Calculator().evaluate("Factorial(170)"), "7257415615307998967396728211129263114716991681296451376543577798900561843401706157852350749242617459511490991237838520776666022565442753025328900773207510902400430280058295603966612599658257104398558294257568966313439612262571094946806711205568880457193340212661452800000000000000000000000000000000000000000");
        
        Assert.equals(new Calculator().evaluate("Factorial(5) == 5!"), "true");
        
        Assert.equals(new Calculator().evaluate("3!"), "6");
        Assert.equals(new Calculator().evaluate("3!!"), "720");
    }
    
    @Test private static void testIntegerLength() {
        Assert.equals(new Calculator().evaluate("IntegerLength(1234)"), "4");
        Assert.equals(new Calculator().evaluate("IntegerLength(100)"), "3");
        Assert.equals(new Calculator().evaluate("IntegerLength(-100)"), "3");
        Assert.equals(new Calculator().evaluate("IntegerLength(170!)"), "307");
    }
    
    @Test private static void testIntegerPartAndFractionalPart() {
        Assert.equals(new Calculator().evaluate("IntegerPart(1.2)"), "1");
        Assert.equals(new Calculator().evaluate("IntegerPart(1)"), "1");
        Assert.equals(new Calculator().evaluate("IntegerPart(-2.4)"), "-2");
        Assert.equals(new Calculator().evaluate("IntegerPart(-2)"), "-2");
        
        Assert.equals(new Calculator().evaluate("FractionalPart(1.2)"), "0.2");
        Assert.equals(new Calculator().evaluate("FractionalPart(1)"), "0.0");
        Assert.equals(new Calculator().evaluate("FractionalPart(-2.4)"), "0.4");
        Assert.equals(new Calculator().evaluate("FractionalPart(-2)"), "0.0");
        
        Assert.equals(new Calculator().evaluate("IntegerPart(1.2) + FractionalPart(1.2)"), "1.2");
    }
    
    @Test private static void testMaxAndMin() {
        Assert.equals(new Calculator().evaluate("Max(-123, 123)"), "123");
        Assert.equals(new Calculator().evaluate("Max(123, 123)"), "123");
        Assert.equals(new Calculator().evaluate("Max(123, 124)"), "124");
        Assert.equals(new Calculator().evaluate("Max(0.1, 0.2)"), "0.2");
        Assert.equals(new Calculator().evaluate("Max(123, 123.1)"), "123.1");
        
        Assert.equals(new Calculator().evaluate("Min(-123, 123)"), "-123");
        Assert.equals(new Calculator().evaluate("Min(123, 123)"), "123");
        Assert.equals(new Calculator().evaluate("Min(123, 124)"), "123");
        Assert.equals(new Calculator().evaluate("Min(0.1, 0.2)"), "0.1");
        Assert.equals(new Calculator().evaluate("Min(123, 123.1)"), "123");
    }
    
    @Test private static void testSign() {
        Assert.equals(new Calculator().evaluate("Sign(-123)"), "-1");
        Assert.equals(new Calculator().evaluate("Sign(-123.0)"), "-1");
        Assert.equals(new Calculator().evaluate("Sign(0)"), "0");
        Assert.equals(new Calculator().evaluate("Sign(0.0)"), "0");
        Assert.equals(new Calculator().evaluate("Sign(123)"), "1");
        Assert.equals(new Calculator().evaluate("Sign(123.0)"), "1");
    }
    
    @Test private static void testSqrt() {
        Assert.equals(new Calculator().evaluate("\u221a4"), "2.0");
        // Check /3*2 == 2*/3 (where / is ASCII-safe \u221a).
        Assert.startsWith(new Calculator().evaluate("\u221a3*2"), "3.464");
    }
    
    @Test private static void testSum() {
        Assert.equals(new Calculator().evaluate("Sum(0, 10, i)"), "55");
        Assert.equals(new Calculator().evaluate("Sum(0, 10.2, i)"), "55.0");
        Assert.equals(new Calculator().evaluate("Sum(0, 10, i^2)"), "385");
        Assert.equals(Double.valueOf(new Calculator().evaluate("Sum(0,30,1/i!)-e")), 0.0, 0.000001);
        // FIXME: failure test for min > max.
    }
    
    @Test private static void testProduct() {
        Assert.equals(new Calculator().evaluate("Product(1, 10, i)"), "3628800");
        Assert.equals(new Calculator().evaluate("Product(1, 10.2, i)"), "3628800.0");
        Assert.equals(new Calculator().evaluate("Product(1, 6, i^2)"), "518400");
        // FIXME: failure test for min > max.
    }
    
    @Test private static void testAns() {
        final Calculator calculator = new Calculator();
        Assert.equals(calculator.evaluate("0"), "0");
        Assert.equals(calculator.evaluate("1+Ans"), "1");
        Assert.equals(calculator.evaluate("1+Ans"), "2");
        Assert.equals(calculator.evaluate("Ans*2"), "4");
        Assert.equals(calculator.evaluate("ans*2"), "8"); // Tests case-insensitivity.
        try {
            new Calculator().evaluate("ans = 3");
            Assert.failure("no exception was thrown when assigning to Ans");
        } catch (CalculatorError ex) {
            Assert.equals(ex.getMessage(), "can't assign a new value to Ans");
        }
    }
    
    @Test private static void testVariables() {
        final Calculator calculator = new Calculator();
        Assert.equals(calculator.evaluate("a = 2"), "2");
        Assert.equals(calculator.evaluate("a"), "2");
        Assert.equals(calculator.evaluate("2*a"), "4");
    }
}