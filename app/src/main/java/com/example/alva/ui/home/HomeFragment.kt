package com.example.alva.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alva.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var foodLogAdapter: FoodLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupUI()
        observeViewModel()

        return binding.root
    }

    private fun setupUI() {
        // Setup RecyclerView for food log
        foodLogAdapter = FoodLogAdapter { foodEntry ->
            // Handle food entry click (edit/delete)
            homeViewModel.deleteFoodEntry(foodEntry)
        }

        binding.recyclerViewFoodLog.apply {
            adapter = foodLogAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Setup click listeners
        binding.fabAddFood.setOnClickListener {
            // Navigate to add food manually or show dialog
            showAddFoodDialog()
        }

        binding.buttonQuickAdd.setOnClickListener {
            // Quick add common foods
            showQuickAddDialog()
        }
    }

    private fun observeViewModel() {
        homeViewModel.dailyCalories.observe(viewLifecycleOwner) { calories ->
            binding.textCurrentCalories.text = calories.toString()
        }

        homeViewModel.calorieGoal.observe(viewLifecycleOwner) { goal ->
            binding.textCalorieGoal.text = "/ $goal"
            updateProgressBar()
        }

        homeViewModel.foodEntries.observe(viewLifecycleOwner) { entries ->
            foodLogAdapter.updateFoodEntries(entries)
        }

        homeViewModel.remainingCalories.observe(viewLifecycleOwner) { remaining ->
            binding.textRemainingCalories.text = if (remaining >= 0) {
                "$remaining calories remaining"
            } else {
                "${Math.abs(remaining)} calories over limit"
            }
        }
    }

    private fun updateProgressBar() {
        val current = homeViewModel.dailyCalories.value ?: 0
        val goal = homeViewModel.calorieGoal.value ?: 2000
        val progress = ((current.toFloat() / goal.toFloat()) * 100).toInt()
        binding.progressBarCalories.progress = minOf(progress, 100)
    }

    private fun showAddFoodDialog() {
        AddFoodDialogFragment { foodName, calories ->
            homeViewModel.addFoodEntry(foodName, calories)
        }.show(parentFragmentManager, "AddFoodDialog")
    }

    private fun showQuickAddDialog() {
        QuickAddDialogFragment { foodEntry ->
            homeViewModel.addFoodEntry(foodEntry.name, foodEntry.calories)
        }.show(parentFragmentManager, "QuickAddDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}