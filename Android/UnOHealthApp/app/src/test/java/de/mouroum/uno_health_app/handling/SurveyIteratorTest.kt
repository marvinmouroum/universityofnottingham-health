package de.mouroum.uno_health_app.handling

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import de.mouroum.uno_health_app.Survey
import org.junit.Test

internal class SurveyIteratorTest {
    @Test
    fun nextSingle() {
        val survey = Gson().fromJson(
            """
{
  "description": "string",
  "id": 0,
  "nameId": "string",
  "questions": [
    {
      "id": 0,
      "order": 0,
      "question": "string",
      "type": "CHOICE"
    }
  ],
  "title": "string",
  "version": 0
}
            """, Survey::class.java
        )

        val surveyIterator = SurveyIterator(survey)

        assertThat(surveyIterator.next(null)?.id).isEqualTo(0)
        assertThat(surveyIterator.next(null)).isNull()
    }


    @Test
    fun nextWihtPosition() {
        val survey = Gson().fromJson(
            """
{
  "description": "string",
  "id": 0,
  "nameId": "string",
  "questions": [
    {
      "id": 0,
      "order": 0,
      "question": "string",
      "type": "CHOICE"
    },
    {
      "id": 1,
      "order": 0,
      "question": "string",
      "type": "CHOICE"
    }
  ],
  "title": "string",
  "version": 0
}
            """, Survey::class.java
        )

        val surveyIterator = SurveyIterator(survey, 1)

        assertThat(surveyIterator.next(null)?.id).isEqualTo(1)
        assertThat(surveyIterator.next(null)).isNull()
    }
}