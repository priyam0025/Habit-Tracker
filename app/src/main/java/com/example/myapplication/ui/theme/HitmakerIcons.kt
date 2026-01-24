package com.example.myapplication.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitIcon(
    val name: String,
    val icon: ImageVector,
    val category: String
)

object HitmakerIcons {
    val icons = listOf(
        // Health
        HabitIcon("Dumbbell", Icons.Rounded.FitnessCenter, "Health"),
        HabitIcon("Running", Icons.Rounded.DirectionsRun, "Health"),
        HabitIcon("Yoga", Icons.Rounded.SelfImprovement, "Health"),
        HabitIcon("Heart", Icons.Rounded.Favorite, "Health"),
        HabitIcon("Water", Icons.Rounded.WaterDrop, "Health"),
        HabitIcon("Steps", Icons.Rounded.DirectionsWalk, "Health"),

        // Study & Work
        HabitIcon("Book", Icons.Rounded.MenuBook, "Study"), // Book might be MenuBook or Book
        HabitIcon("Notebook", Icons.Rounded.Note, "Study"),
        HabitIcon("Graduation", Icons.Rounded.School, "Study"),
        HabitIcon("Laptop", Icons.Rounded.Computer, "Study"),
        HabitIcon("Code", Icons.Rounded.Code, "Study"),
        HabitIcon("Brain", Icons.Rounded.Psychology, "Study"),

        // Mindfulness
        HabitIcon("Meditation", Icons.Rounded.Spa, "Mindfulness"),
        HabitIcon("Lotus", Icons.Rounded.LocalFlorist, "Mindfulness"),
        HabitIcon("Candle", Icons.Rounded.LightMode, "Mindfulness"), // Approx
        HabitIcon("Moon", Icons.Rounded.Bedtime, "Mindfulness"),
        HabitIcon("Breath", Icons.Rounded.Air, "Mindfulness"),

        // Lifestyle
        HabitIcon("Apple", Icons.Rounded.Restaurant, "Lifestyle"), // Generic food
        HabitIcon("Leaf", Icons.Rounded.Eco, "Lifestyle"),
        HabitIcon("Bed", Icons.Rounded.Bed, "Lifestyle"),
        HabitIcon("Alarm", Icons.Rounded.Alarm, "Lifestyle"),
        HabitIcon("Sun", Icons.Rounded.WbSunny, "Lifestyle"),

        // Creative
        HabitIcon("Music", Icons.Rounded.MusicNote, "Creative"),
        HabitIcon("Brush", Icons.Rounded.Brush, "Creative"),
        HabitIcon("Camera", Icons.Rounded.PhotoCamera, "Creative"),
        HabitIcon("Pencil", Icons.Rounded.Edit, "Creative"),
        HabitIcon("Mic", Icons.Rounded.Mic, "Creative"),

        // Generic
        HabitIcon("Check", Icons.Rounded.CheckCircle, "Generic"),
        HabitIcon("Star", Icons.Rounded.Star, "Generic"),
        HabitIcon("Target", Icons.Rounded.TrackChanges, "Generic"),
        HabitIcon("Flame", Icons.Rounded.LocalFireDepartment, "Generic"),
        HabitIcon("Calendar", Icons.Rounded.CalendarMonth, "Generic"),
        HabitIcon("Flag", Icons.Rounded.Flag, "Generic"),
        HabitIcon("Love", Icons.Rounded.Favorite, "Generic"),
        HabitIcon("Sparkling", Icons.Rounded.AutoAwesome, "Health"),
        HabitIcon("Gooning", Icons.Rounded.Visibility, "Generic"),
        HabitIcon("Gaming", Icons.Rounded.Gamepad, "Lifestyle"),
        HabitIcon("Food", Icons.Rounded.Fastfood, "Lifestyle"),
        HabitIcon("Work", Icons.Rounded.Work, "Study"),
        HabitIcon("Journal", Icons.Rounded.HistoryEdu, "Creative"),
        HabitIcon("Art", Icons.Rounded.Palette, "Creative"),
        HabitIcon("Home", Icons.Rounded.Home, "Lifestyle"),
        HabitIcon("Pets", Icons.Rounded.Pets, "Lifestyle"),
        HabitIcon("Energy", Icons.Rounded.Bolt, "Generic"),
        HabitIcon("Timer", Icons.Rounded.Timer, "Generic"),
        HabitIcon("Movie", Icons.Rounded.Movie, "Creative"),
        HabitIcon("Money", Icons.Rounded.ShoppingBag, "Lifestyle")
    )

    fun getIcon(name: String): ImageVector {
        return icons.find { it.name == name }?.icon ?: Icons.Rounded.Star // Default
    }
}
