package de.interoberlin.lymbo.view.activities

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import de.interoberlin.lymbo.App.Companion.context
import de.interoberlin.lymbo.R
import de.interoberlin.lymbo.controller.CardsController
import de.interoberlin.lymbo.view.adapters.CardsRecyclerViewAdapter
import de.interoberlin.lymbo.view.dialogs.CardDialog
import de.interoberlin.lymbo.view.dialogs.TagDialog
import de.interoberlin.lymbo.view.helper.OnStartDragListener
import de.interoberlin.lymbo.view.helper.SimpleItemTouchHelperCallback


class CardsActivity : AppCompatActivity(), OnStartDragListener {
    companion object {
        val TAG = CardsActivity::class.toString()
        lateinit var cardsAdapter: CardsRecyclerViewAdapter
    }

    private val controller = CardsController.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cards)
        title = "${context.resources.getString(R.string.app_name)} | ${controller.stack.title}"

        controller.updateTags()
    }

    override fun onResume() {
        super.onResume()

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        val fab = findViewById(R.id.fab) as FloatingActionButton
        val rvCards = findViewById(R.id.rvCards) as RecyclerView

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cardsAdapter = CardsRecyclerViewAdapter(controller.cards)
        rvCards.layoutManager = LinearLayoutManager(this)
        rvCards.adapter = cardsAdapter

        fab.setOnClickListener { _ ->
            val dialog = CardDialog()
            val bundle = Bundle()
            bundle.putString(context.resources.getString(R.string.bundle_card), null)
            bundle.putString(context.resources.getString(R.string.bundle_tags), Gson().toJson(controller.tags))
            dialog.arguments = bundle
            dialog.isCancelable = false
            dialog.cardAddSubject.subscribe { card ->
                controller.addCard(card)
            }
            dialog.show(fragmentManager, CardDialog.TAG)
        }

        controller.cardsSubject.subscribe { _ ->
            rvCards.adapter = null
            rvCards.layoutManager = null
            rvCards.adapter = cardsAdapter
            rvCards.layoutManager = LinearLayoutManager(this)

            cardsAdapter.notifyDataSetChanged()
        }

        controller.cardsFilterSubject.subscribe { _ ->
            controller.updateTags()
            cardsAdapter.applyFilter("")
        }


        val callback = SimpleItemTouchHelperCallback(cardsAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvCards)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cards, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.action_tags -> {
                val dialog = TagDialog()
                val bundle = Bundle()
                bundle.putString(context.resources.getString(R.string.bundle_tags), Gson().toJson(controller.tags))
                dialog.arguments = bundle
                dialog.isCancelable = true
                dialog.tagsSelectedSubject.subscribe { tags ->
                    controller.tags = tags
                    cardsAdapter.applyFilter("")
                }
                dialog.show(fragmentManager, TagDialog.TAG)
            }
            R.id.action_recover -> controller.cards.forEach { c ->
                c.checked = false
                cardsAdapter.applyFilter("")
            }
            R.id.action_settings -> {
                val main = findViewById(R.id.layoutMain)
                showSnackbar(main, "Clicked on menu item Settings")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
    }
}
