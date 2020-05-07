package de.mouroum.uno_health_app

import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout


class MainAdapter(context: MainActivity) : BaseAdapter() {

    private val mContext: MainActivity = context

    var question: Question? = null

    var selectedChoice = mutableListOf<Int>()
    var selectedBool: Boolean? = null
    var selectedRange: Int? = null
    var selectedText: String? = null
    var selectedChecklist = mutableMapOf<Long, Boolean>()

    fun setCurrentQuestion(q: Question) {
        this.question = q

        this.selectedChoice = mutableListOf<Int>()
        this.selectedBool = null
        this.selectedRange = null
        this.selectedText = null
        this.selectedChecklist = mutableMapOf<Long, Boolean>()

        notifyDataSetChanged()
    }

    override fun getCount(): Int {

        if (question == null) return 0

        return when (question!!.type) {
            AnswerType.CHOICE -> question?.answers?.size ?: 0
            AnswerType.BOOL -> 2
            AnswerType.CHECKLIST -> question?.entries?.size ?: 0
            AnswerType.TEXT -> 1
            AnswerType.RANGE -> 1
        }
    }

    override fun getItem(position: Int): Any {
        return ""
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val layoutInActivity = LayoutInflater.from(mContext)

        if (question == null) return null

        return when (question!!.type) {
            AnswerType.CHOICE -> getChoiceView(position, parent, layoutInActivity)
            AnswerType.BOOL -> getBoolView(position, parent, layoutInActivity)
            AnswerType.RANGE -> getSliderView(position, parent, layoutInActivity)
            AnswerType.TEXT -> getTextView(position, parent, layoutInActivity)
            AnswerType.CHECKLIST -> getChecklistView(position, parent, layoutInActivity)
        }
    }

    private fun getSliderView(
        position: Int,
        parent: ViewGroup?,
        layoutInActivity: LayoutInflater
    ): View? {

        val min = question!!.minValue
        val max = question!!.maxValue
        val step = 1

        val cell = layoutInActivity.inflate(R.layout.slider_question, parent, false)
        val seekbar = cell.findViewById<SeekBar>(
            R.id.seekBar
        )
        val minValueText = cell.findViewById<TextView>(
            R.id.minValueText
        )
        val maxValueText = cell.findViewById<TextView>(
            R.id.maxValueText
        )
        val minText = cell.findViewById<TextView>(
            R.id.minText
        )
        val maxText = cell.findViewById<TextView>(
            R.id.maxText
        )

        minValueText.text = "$min"
        maxValueText.text = "$max"
        minText.text = question!!.minText
        maxText.text = question!!.maxText
        seekbar.max = (max - min) / step

        seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    selectedRange = min + progress * step
                }
            }
        )

        if (parent != null) {
            cell.layoutParams.height = parent.height
            cell.requestLayout()
        }

        return cell
    }

    private fun getTextView(
        position: Int,
        parent: ViewGroup?,
        layoutInActivity: LayoutInflater
    ): View {

        val cell = layoutInActivity.inflate(R.layout.text_question, parent, false)
        val editor = cell.findViewById<EditText>(R.id.editText)
        val textLength = cell.findViewById<TextView>(R.id.textLength)

        editor.isSingleLine = !question!!.multiline

        if (selectedText != null) {
            editor.setText(selectedText, TextView.BufferType.EDITABLE)
            val text = "${selectedText!!.length} / ${question!!.length}"
            textLength.text = text
        }

        val watcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                selectedText = s.toString()
                val text = "${s.toString().length} / ${question!!.length}"
                textLength.text = text

                println(selectedText)
            }

            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {}

            override fun onTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {}
        }

        editor.addTextChangedListener(watcher)
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(question!!.length)
        editor.filters = filterArray

        if (parent != null) {
            cell.layoutParams.height = parent.height
            cell.requestLayout()
        }

        return cell
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun getChoiceView(
        position: Int,
        parent: ViewGroup?,
        layoutInActivity: LayoutInflater
    ): View {

        val cell = layoutInActivity.inflate(R.layout.multiple_choice_question, parent, false)
        val textview = cell.findViewById<TextView>(
            R.id.answerTextView
        )
        val constLayOut = textview.layoutParams as ConstraintLayout.LayoutParams

        textview.text = question!!.answers[position].value

        cell.setOnClickListener {
            if (selectedChoice.contains(position)) {
                selectedChoice.remove(position)
            } else {
                if (question!!.multiple)
                    selectedChoice.add(position)
                else {
                    selectedChoice.clear()
                    selectedChoice.add(position)
                }
            }
            this@MainAdapter.notifyDataSetChanged()
        }

        if (selectedChoice.contains(position)) {
            constLayOut.leftMargin = 20
            textview.setPadding(0, 0, 0, 0)
        } else {
            constLayOut.leftMargin = 0
            textview.setPadding(20, 0, 0, 0)
        }
        textview.layoutParams = constLayOut

        return cell
    }

    private fun getBoolView(
        position: Int,
        parent: ViewGroup?,
        layoutInActivity: LayoutInflater
    ): View {
        val answer =
            if (position == 1) mContext.resources.getString(R.string.answer_no)
            else mContext.resources.getString(R.string.answer_yes)

        val cell = layoutInActivity.inflate(R.layout.multiple_choice_question, parent, false)
        val textview = cell.findViewById<TextView>(
            R.id.answerTextView
        )
        val constLayOut = textview.layoutParams as ConstraintLayout.LayoutParams

        textview.text = answer

        cell.setOnClickListener {
            selectedBool = position == 0
            this@MainAdapter.notifyDataSetChanged()
        }

        if (selectedBool != null && (position == 0 && selectedBool == true || position == 1 && selectedBool == false)) {
            constLayOut.leftMargin = 20
            textview.setPadding(0, 0, 0, 0)
        } else {
            constLayOut.leftMargin = 0
            textview.setPadding(20, 0, 0, 0)
        }
        textview.layoutParams = constLayOut

        return cell
    }

    private fun getChecklistView(
        position: Int,
        parent: ViewGroup?,
        layoutInActivity: LayoutInflater
    ): View {

        val cell = layoutInActivity.inflate(R.layout.checklist_question, parent, false)
        val checkBox = cell.findViewById<CheckBox>(R.id.checkBox)

        checkBox.text = question!!.entries[position].question

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                selectedChecklist[question!!.entries[position].id] = true
            else
                selectedChecklist.remove(question!!.entries[position].id)
        }

        val value = selectedChecklist[question!!.entries[position].id]
        checkBox.isChecked = value == true

        return cell
    }
}