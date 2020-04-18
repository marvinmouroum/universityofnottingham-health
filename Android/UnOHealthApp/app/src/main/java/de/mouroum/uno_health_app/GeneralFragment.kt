package de.mouroum.uno_health_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class GeneralFragment(layout:Int):Fragment() {

    //2
    companion object {

        fun newInstance(id:Int): GeneralFragment {

            val instance = GeneralFragment(id)

            return instance
        }
    }

    var layout:Int

    init {
        this.layout = layout
    }

    //3
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(this.layout, container, false)
    }


}