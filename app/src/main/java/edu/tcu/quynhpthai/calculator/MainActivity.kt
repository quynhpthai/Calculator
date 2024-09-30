package edu.tcu.quynhpthai.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Stack

class MainActivity : AppCompatActivity() {
    private var resultTv: TextView? = null
    private val currentInput = StringBuilder() // Holds the current input

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultTv = findViewById(R.id.result_tv) // TextView to display result
    }

    // Method to handle digit button clicks
    fun onDigitClick(view: View) {
        val clickedButton = view as Button
        val digit = clickedButton.text.toString()

        if (digit == "." && currentInput.toString().contains(".")) {
            return  // Prevent multiple dots in a single number
        }

        currentInput.append(digit)
        resultTv!!.text = currentInput.toString()
    }

    // Method to handle operator button clicks
    fun onOperatorClick(view: View) {
        val clickedButton = view as Button
        val operator = clickedButton.text.toString()

        // Ensure operator is valid
        if (currentInput.length > 0) {
            val lastChar = currentInput[currentInput.length - 1]
            if (lastChar != '+' && lastChar != '-' && lastChar != '*' && lastChar != '/') {
                currentInput.append(operator)
                resultTv!!.text = currentInput.toString()
            }
        }
    }

    // Method to clear input
    fun onClearClick(view: View?) {
        currentInput.setLength(0) // Clear input
        resultTv!!.text = "0" // Reset display
    }

    // Method to handle the equals button
    fun onEqualClick(view: View?) {
        try {
            // Step 1: Convert to postfix
            val postfix = infixToPostfix(currentInput.toString())

            // Step 2: Evaluate postfix expression
            val result = evaluatePostfix(postfix)

            resultTv!!.text = result.toString() // Display the result
            currentInput.setLength(0) // Clear current input
        } catch (e: Exception) {
            resultTv!!.text = "Error" // Handle errors in expression
        }
    }

    // Check if a character is an operator
    private fun isOperator(ch: Char): Boolean {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/'
    }

    // Get precedence of operators
    private fun precedence(operator: Char): Int {
        return when (operator) {
            '+', '-' -> 1
            '*', '/' -> 2
            else -> -1
        }
    }

    // Convert infix expression to postfix expression (Shunting Yard Algorithm)
    private fun infixToPostfix(expression: String): String {
        val stack = Stack<Char>()
        val postfix = StringBuilder()
        val numberBuffer = StringBuilder()

        for (i in 0 until expression.length) {
            val ch = expression[i]

            // If character is a digit or a dot (decimal point)
            if (Character.isDigit(ch) || ch == '.') {
                numberBuffer.append(ch) // Collect the number
            } else if (isOperator(ch)) {
                // Append the number we've been collecting to postfix
                if (numberBuffer.length > 0) {
                    postfix.append(numberBuffer).append(" ")
                    numberBuffer.setLength(0) // Clear the buffer
                }

                // Pop operators with higher or equal precedence
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(ch)) {
                    postfix.append(stack.pop()).append(" ")
                }

                stack.push(ch) // Push the current operator onto the stack
            }
        }

        // Append the last number in the buffer, if any
        if (numberBuffer.length > 0) {
            postfix.append(numberBuffer).append(" ")
        }

        // Pop all the remaining operators from the stack
        while (!stack.isEmpty()) {
            postfix.append(stack.pop()).append(" ")
        }

        return postfix.toString().trim { it <= ' ' } // Return the postfix expression
    }

    // Evaluate a postfix expression
    private fun evaluatePostfix(postfix: String): Double {
        val stack = Stack<Double>()
        val tokens = postfix.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        for (token in tokens) {
            if (isOperator(token[0]) && token.length == 1) {
                // Operator: pop two operands, apply the operator, and push the result
                val b = stack.pop()
                val a = stack.pop()
                when (token[0]) {
                    '+' -> stack.push(a + b)
                    '-' -> stack.push(a - b)
                    '*' -> stack.push(a * b)
                    '/' -> {
                        if (b == 0.0) throw ArithmeticException("Divide by zero")
                        stack.push(a / b)
                    }
                }
            } else {
                // Operand: parse it as a double and push onto the stack
                stack.push(token.toDouble())
            }
        }

        // The final result will be on the top of the stack
        return stack.pop()
    }
}