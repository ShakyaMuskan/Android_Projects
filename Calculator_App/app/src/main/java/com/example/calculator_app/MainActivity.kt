package com.example.calculator_app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator_app.databinding.ActivityMainBinding
import java.math.BigDecimal


class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var start = false
    private val enter = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
        val actionBar = supportActionBar

        // Check if ActionBar is not null before showing it
        actionBar?.show()
        binding!!.buttonAc.setOnClickListener(View.OnClickListener {
            binding!!.solutionTv.setText(" ")
            binding!!.inputTv.setText("0")
            start = true
        })
        binding!!.button0.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("0")
                start = false
            } else {
                binding!!.inputTv.append("0")
            }
        })
        binding!!.button1.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("1")
                start = false
            } else {
                binding!!.inputTv.append("1")
            }
        })
        binding!!.button2.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("2")
                start = false
            } else {
                binding!!.inputTv.append("2")
            }
        })
        binding!!.button3.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("3")
                start = false
            } else {
                binding!!.inputTv.append("3")
            }
        })
        binding!!.button4.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("4")
                start = false
            } else {
                binding!!.inputTv.append("4")
            }
        })
        binding!!.button5.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("5")
                start = false
            } else {
                binding!!.inputTv.append("5")
            }
        })
        binding!!.button6.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("6")
                start = false
            } else {
                binding!!.inputTv.append("6")
            }
        })
        binding!!.button6.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("6")
                start = false
            } else {
                binding!!.inputTv.append("6")
            }
        })
        binding!!.button7.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("7")
                start = false
            } else {
                binding!!.inputTv.append("7")
            }
        })
        binding!!.button8.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("8")
                start = false
            } else {
                binding!!.inputTv.append("8")
            }
        })
        binding!!.button9.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("9")
                start = false
            } else {
                binding!!.inputTv.append("9")
            }
        })
        binding!!.buttonCloseBracket.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText(")")
                start = false
            } else {
                binding!!.inputTv.append(")")
            }
        })
        binding!!.buttonOpenBracket.setOnClickListener(View.OnClickListener {
            if (binding!!.inputTv.getText().toString().equals("0") || start) {
                binding!!.inputTv.setText("(")
                start = false
            } else {
                binding!!.inputTv.append("(")
            }
        })
        binding!!.buttonC.setOnClickListener(View.OnClickListener {
            val currentInp: String = binding!!.inputTv.getText().toString()
            if (currentInp.length > 1) {
                binding!!.inputTv.setText(currentInp.substring(0, currentInp.length - 1))
            } else {
                binding!!.inputTv.setText("0")
                start = false
            }
        })
        binding!!.buttonPlus.setOnClickListener(View.OnClickListener {
            val currentInput: String = binding!!.inputTv.getText().toString()
            val last = currentInput[currentInput.length - 1]
            if (Character.isDigit(last)) {
                binding!!.inputTv.append("+")
            } else {
                //last='+';
                checkOperator("+", last)
            }
        })
        binding!!.buttonMinus.setOnClickListener(View.OnClickListener {
            val currentInput: String = binding!!.inputTv.getText().toString()
            val last = currentInput[currentInput.length - 1]
            if (Character.isDigit(last)) {
                binding!!.inputTv.append("-")
            } else {
                //last='+';
                checkOperator("-", last)
            }
        })
        binding!!.buttonMultiply.setOnClickListener(View.OnClickListener {
            val currentInput: String = binding!!.inputTv.getText().toString()
            val last = currentInput[currentInput.length - 1]
            if (Character.isDigit(last)) {
                binding!!.inputTv.append("*")
            } else {
                //last='+';
                checkOperator("*", last)
            }
        })
        binding!!.buttonDivide.setOnClickListener(View.OnClickListener {
            val currentInput: String = binding!!.inputTv.getText().toString()
            val last = currentInput[currentInput.length - 1]
            if (Character.isDigit(last)) {
                binding!!.inputTv.append("/")
            } else {
                //last='+';
                checkOperator("/", last)
            }
        })
        binding!!.buttonEquals.setOnClickListener(View.OnClickListener {
            val dataToCalculate: String = binding!!.inputTv.text.toString()
            try {
                val expression: Expression = ExpressionBuilder(dataToCalculate).build()
                val result = expression.evaluate()

                // Convert the result to String before displaying
                val resultString = result.toString()
                binding!!.solutionTv.text = resultString
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Invalid Expression", Toast.LENGTH_SHORT).show()
            }
        })


    }
       private fun checkOperator(str: String, last: Char) {
        if (Character.toString(last) == str) {
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show()
        } else {
            var currentInput: String = binding!!.inputTv.getText().toString()
            currentInput = currentInput.substring(0, currentInput.length - 1) + str
            binding!!.inputTv.setText(currentInput)
        }
    }
    }
