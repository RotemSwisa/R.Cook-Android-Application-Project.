package com.kinneret.rcook.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.UUID;
import java.util.concurrent.Executors;

@Database(entities = {Lesson.class, User.class}, version = 1, exportSchema = false)
public abstract class RCookRoomDatabase extends RoomDatabase {

    // Define DAOs
    public abstract LessonDao lessonDao();
    public abstract UserDao userDao();

    // Singleton instance
    private static volatile RCookRoomDatabase INSTANCE;

    // Get database instance (singleton pattern)
    public static RCookRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RCookRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RCookRoomDatabase.class, "rcook_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Database callback to populate with sample data
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Use Executor instead of AsyncTask
            Executors.newSingleThreadExecutor().execute(() -> {
                // Populate database with sample data if it's empty
                LessonDao lessonDao = INSTANCE.lessonDao();
                UserDao userDao = INSTANCE.userDao();

                // Check if database is empty and populate with sample data
                if (lessonDao.getAnyLesson().length < 1) {
                    populateSampleLessons(lessonDao);
                }

                // Create default user if doesn't exist
                if (userDao.getUserCount() == 0) {
                    User defaultUser = new User();
                    userDao.insertOrUpdate(defaultUser);
                }
            });
        }
    };

    // Helper method for populating main classes
    private static void populateSampleLessons(LessonDao lessonDao) {
        // Beginners lessons
        lessonDao.insert(new Lesson(
                UUID.randomUUID().toString(),
                    "Crispy Schnitzel",
                    "Chef Hadar",
                    "We will learn to make crispy schnitzel.",
                    "Okay, so among my favorite foods is schnitzel, but of course you have to know how to prepare it properly.\n" +
                            "I've tried a million methods for making schnitzel, I even tried watching the flour before the batter, but what happened during frying was that the breadcrumbs separated from the chicken breast.\n" +
                            "I like my schnitzel not too thick but not too thin because then you only feel breadcrumbs.\n" +
                            "I'll write down some tips for a successful and crispy schnitzel.\n" +
                            "Leave the chicken breast in the batter for about an hour (to absorb flavors)\n" +
                            "You can even put it in the refrigerator overnight.\n" +
                            "Fry in hot, deep oil, which ensures crispy schnitzel.\n" +
                            "Press the chicken breast with your wrist in the breadcrumbs so that they stick well to the chicken breast.\n" +
                            "After frying, transfer the schnitzels to a cooling rack.\n" +
                            "1/2 kilo sliced chicken breast\n" +
                            "\n" +
                            "For the batter:\n" +
                            "Egg L\n" +
                            "•\tA heaped teaspoon of Dijon mustard grains\n" +
                            "•\tA heaped teaspoon of mayonnaise\n" +
                            "•\tA teaspoon of crushed garlic\n" +
                            "•\tA teaspoon of salt\n" +
                            "•\tA flat teaspoon of flour\n" +
                            "•\tA teaspoon of paprika\n" +
                            "•\t1/4 teaspoon of black pepper\n" +
                            "•\t1/2 teaspoon of cumin\n" +
                            "•\tA tablespoon of water\n" +
                            "\n" +
                            "For coating:\n" +
                            "•\tBread crumbs (almost a package)\n" +
                            "•\t3-4 tablespoons of sesame seeds.\n" +
                            "•\t1/2 teaspoon of salt\n" +
                            "\n" +
                            "For frying:\n" +
                            "•\tA wide frying pan/pot\n" +
                            "•\tOil for deep frying - 3/4 bottle of oil.\n" +
                            "\n" +
                            "How to prepare:\n" +
                            "Wash the sliced chicken breast well and cut it in half (you can also leave it as it is, I just like relatively small schnitzels)\n" +
                            "In a bowl, mix the egg, water, mayonnaise, crushed garlic mustard, paprika and all the other spices with a whisk or fork.\n" +
                            "We will transfer the chicken breast to the batter, mix, cover with plastic wrap, and transfer to the refrigerator for about an hour or overnight in the refrigerator.\n" +
                            "After an hour/overnight, we will spread baking paper/aluminum foil\n" +
                            "on the work surface and pour sesame bread crumbs and salt, mix lightly.\n" +
                            "We will start coating the schnitzel.\n" +
                            "I like to work in the following method\n" +
                            "One hand is dry for coating, the second hand is for the chicken breast. This means\n" +
                            "that the first hand takes a piece of chicken breast from the batter, places it on the bread crumbs, and the second (dry) hand begins to coat the chicken breast well with the wrist\n" +
                            "The hand that finished one side turns the schnitzel\n" +
                            "and presses well on the other side as well.\n" +
                            "Once we do this, it also tightens the schnitzel and is one of the tips for crispy schnitzel. This is how we will repeat the operation with the rest of the schnitzels.\n" +
                            "\n" +
                            "Let's start frying:\n" +
                            "In a wide pot/pan, fold baking paper open and place it on the pan (it's easier to clean up after it and it takes time for the oil to burn)\n" +
                            "We'll pour the oil and turn on the gas to a medium flame and wait until the oil is hot.\n" +
                            "How do you check? Take a wooden spoon and place it on the oil. As soon as bubbles form around the spoon, it means the oil is hot enough and you can start frying.\n" +
                            "Let each schnitzel cook for about 2-3 minutes on each side until golden brown.\n" +
                            "We'll transfer the schnitzels straight after frying to a cooling rack.\n" +
                            "\n" +
                            "Enjoy ☺️\n",
                    "drawable/schnitzel",
                    "schnitzel_video",
                    "Beginners"
            ));

        lessonDao.insert(new Lesson(
                    UUID.randomUUID().toString(),
                    "Salmon fillet",
                    "Chef Hadar",
                    "Honey and mustard salmon in the oven.",
                    "What you need:\n" +
                            "•\tSlice of salmon fillet\n" +
                            "•\t15 cherry tomatoes\n" +
                            "•\t10 lemon slices from 2 lemons\n" +
                            "\n" +
                            "For the marinade:\n" +
                            "•\tA bunch of coriander (without the stem)\n" +
                            "•\t3 tablespoons honey\n" +
                            "•\t2 tablespoons Dijon mustard grains\n" +
                            "•\t3 tablespoons canola oil\n" +
                            "•\tA teaspoon of crushed garlic\n" +
                            "•\tA teaspoon of coarsely ground black pepper\n" +
                            "•\tA teaspoon of fine salt\n" +
                            "\n" +
                            "How to prepare:\n" +
                            "Preheat the oven to 190% turbo half an hour before.\n" +
                            "Cut lemon slices and arrange them on the pan, add the fish slice and the cherry tomatoes on top.\n" +
                            "Mix all the marinade ingredients and brush over the fish.\n" +
                            "Squeeze the juice from half a lemon over the fish.\n" +
                            "Put in the oven in the middle stage for about 20-22 minutes, no more than a minute otherwise it will dry out.\n" +
                            "Remove from the oven and drizzle a little more honey over the top. It is recommended to serve with mashed potatoes and fried onions.\n" +
                            "Enjoy your meal ☺️\n",
                    "drawable/salmon",
                    "salmon_video",
                    "Beginners"
            ));

        lessonDao.insert(new Lesson(
                    UUID.randomUUID().toString(),
                    "Pasta Rosa",
                    "Chef Hadar",
                    "Pasta with creamy and spicy rose sauce.",
                    "Pasta with a spicy creamy rose sauce!\n" +
                            "Enough for 2 servings\n" +
                            "\n" +
                            "Ingredients:\n" +
                            "•\t150 grams pasta\n" +
                            "•\t480 ml boiling water\n" +
                            "•\t38% 120 ml sweet cream\n" +
                            "•\t3 heaping tablespoons tomato paste\n" +
                            "•\t3 crushed garlic cloves\n" +
                            "•\t1/4 chopped hot pepper (optional)\n" +
                            "•\t40 grams butter\n" +
                            "•\t2 tablespoons olive oil\n" +
                            "•\t1 teaspoon sweet paprika\n" +
                            "•\t1/4 teaspoon black pepper\n" +
                            "•\t1/2 teaspoon chili\n" +
                            "•\t1/2 teaspoon salt\n" +
                            "•\t1/2 teaspoon sugar\n" +
                            "•\t1/4 teaspoon oregano\n" +
                            "•\t2 slices of goat cheese\n" +
                            "•\tParmesan\n" +
                            "\n" +
                            "Preparation:\n" +
                            "In a wide frying pan/pot over medium heat, heat the butter until melted and add the olive oil.\n" +
                            "Add the crushed garlic and fry until lightly golden, then add the hot pepper and mix.\n" +
                            "Add the paste Tomatoes, paprika and other spices and stir for about a minute.\n" +
                            "Add the pasta and stir.\n" +
                            "Add the water to cover and cook for 15-20 minutes until the pasta is tender. (You may need to add a little more water)\n" +
                            "Important*During the cooking phase, stir every 3-4 minutes so that the pasta does not stick to the bottom.\n" +
                            "Add the sweet cream, stir and cook for another five minutes.\n" +
                            "Add the yellow Parmesan cheese and stir.\n" +
                            "Serve immediately with more Parmesan on top.\n" +
                            "\n" +
                            "Enjoy ☺️\n",
                    "drawable/pasta",
                    "pasta_video",
                    "Beginners"
            ));



            // Medium lessons
        lessonDao.insert(new Lesson(
                    UUID.randomUUID().toString(),
                    "Meat tortilla",
                    "Chef Sandra",
                    "Fried meat-stuffed tortilla.",
                    "Ingredients\n" +
                            "•\tMedium onion\n" +
                            "•\t2500 grams of minced meat\n" +
                            "•\tSalt, black pepper, paprika, cumin and turmeric\n" +
                            "•\tTortas cut into 2\n" +
                            "•\tFlour and water for a sticky mixture\n" +
                            "•\tFor coating: egg + breadcrumbs\n" +
                            "•\tOil for frying\n" +
                            "\n" +
                            "Preparation\n" +
                            "Fry the onion until golden\n" +
                            "Add spices and mix\n" +
                            "Add the minced meat, mix until the meat\n" +
                            "changes color and remove from heat\n" +
                            "Cut each tortilla into 2 equal pieces\n" +
                            "Fill the tortilla and apply the\n" +
                            "flour and water mixture to the edge so that it sticks\n" +
                            "Dip each tortilla in egg and breadcrumbs\n" +
                            "And fry in hot oil\n" +
                            "\n" +
                            "Recommended with tahini and salad on the side.\n" +
                            "Enjoy ☺️\n.",
                    "drawable/tortilla",
                    "tortilla_video",
                    "Medium"
            ));

        lessonDao.insert(new Lesson(
                    UUID.randomUUID().toString(),
                    "Crispy chicken",
                    "Chef Sandra",
                    "Crispy tempura fried chicken strips.",
                    "Ingredients\n" +
                            "•\t300 grams of chicken breast.\n" +
                            "•\tEgg\n" +
                            "•\tHalf a cup of regular flour and a tablespoon of baking powder\n" +
                            "•\tHalf a cup of water\n" +
                            "•\tSalt Black pepper, garlic powder and paprika Half a bottle of oil for frying\n" +
                            "\n" +
                            "For the dry ingredients\n" +
                            "It largely depends on the amount of chicken breast, but the ratio is For each tablespoon of cornflour = one and a half tablespoons of flour and a little salt to add to the dry ingredients\n" +
                            "\n" +
                            "Preparation\n" +
                            "Cut the chicken breast into strips\n" +
                            "In a bowl, mix the ingredients\n" +
                            "(egg, flour, baking powder, water and spices)\n" +
                            "Put the chicken strips in the batter and let it soak for 10-15 minutes\n" +
                            "Heat in a deep frying pan oil\n" +
                            "When the oil is hot, you can put the chicken breast\n" +
                            "In the dry mixture (the flour, cornflour and salt)\n" +
                            "and put it in the oil. Fry until browned.\n" +
                            "It is recommended to eat with a dip that you like\n" +
                            "\n" +
                            "Enjoy ☺️\n.",
                    "drawable/crispy_chicken",
                    "crispy_chicken_video",
                    "Medium"
            ));

        lessonDao.insert(new Lesson(
                    UUID.randomUUID().toString(),
                    "Kinder Rolls",
                    "Chef Dolev",
                    "Rolled cookies with Kinder chocolate.",
                    "Ingredients\n" +
                            "•\t4 cups flour (disposable cup hot drink)\n" +
                            "•\t1.5 cups powdered sugar\n" +
                            "•\t250 grams butter\n" +
                            "•\t2 tablespoons oil\n" +
                            "•\t2 eggs size L\n" +
                            "\n" +
                            "Filling:\n" +
                            "•\tNutella spread Kinder fingers\n" +
                            "\n" +
                            "Preparation method:\n" +
                            "Suitable for 2 rolls.\n" +
                            "In the bowl of a mixer with a guitar hook or in a food processor, place butter at room temperature cut into cubes, flour,\n" +
                            "and powdered sugar, until a crumbly texture is obtained.\n" +
                            "Then add the oil and eggs, and mix until you get a uniform dough\n" +
                            "The dough is easy to work with, no need to refrigerate - you can\n" +
                            "Start by dividing the dough into two equal parts.\n" +
                            "Roll each part between 2 lightly floured baking sheets\n" +
                            "Not too thin and not too thick\n" +
                            "Spread the Nutella on the dough, arrange Kinder fingers at the edge and roll with baking paper into a roll Put in the freezer for 20 minutes\n" +
                            "In the meantime, preheat the oven to 150 degrees top and bottom heat\n" +
                            "Then bake for about 20 minutes.\n" +
                            "Until they set but remain relatively light\n" +
                            "Cool the rolls completely before cutting. Sprinkle powdered sugar generously on top and cut with a serrated knife.\n" +
                            "\n" +
                            "Enjoy ☺️\n.",
                    "drawable/kinder_rolls",
                    "kinder_rolls_video",
                    "Medium"
            ));

            // Advanced lessons
        lessonDao.insert(new Lesson(
                    UUID.randomUUID().toString(),
                    "Cuba beet",
                    "Chef Hadar",
                    "Meat-stuffed cubes with red beet sauce.",
                    "Ingredients for the soup:\n" +
                            "•\t2 onions, cut into strips\n" +
                            "•\t4 cloves of garlic, crushed\n" +
                            "•\t4 medium beets (2 diced\n" +
                            "•\t2 grated)\n" +
                            "•\t3 celery stalks + leaves, coarsely chopped\n" +
                            "•\t2 tablespoons concentrated tomato paste\n" +
                            "•\t1/2 teaspoon cumin\n" +
                            "•\t1/2 teaspoon black pepper\n" +
                            "•\t1 teaspoon allspice\n" +
                            "•\t1 heaped teaspoon sweet paprika\n" +
                            "•\t2 tablespoons sugar\n" +
                            "•\t1 teaspoon salt\n" +
                            "•\t1.5 liters of water\n" +
                            "•\tJuice from half a lemon\n" +
                            "\n" +
                            "Ingredients for the casing:\n" +
                            "•\t2 and a half cups of semolina\n" +
                            "•\t3 tablespoons of oil\n" +
                            "•\t1 teaspoon of salt\n" +
                            "•\t1 cup of water\n" +
                            "\n" +
                            "Ingredients for the filling:\n" +
                            "•\t500 grams of ground meat + 100 grams of fat*important*\n" +
                            "•\t1 onion, diced into small cubes\n" +
                            "•\tA handful of chopped celery leaves\n" +
                            "•\t2 tablespoons of oil\n" +
                            "•\t1 teaspoon of paprika\n" +
                            "•\t1/4 teaspoon of turmeric\n" +
                            "•\t1/4 teaspoon Cinnamon\n" +
                            "•\t1/2 teaspoon cumin\n" +
                            "•\t1/2 teaspoon allspice\n" +
                            "•\t1/2 teaspoon black pepper\n" +
                            "•\t1 teaspoon salt\n" +
                            "\n" +
                            "Preparation method:\n" +
                            "Start with the filling in a pan over medium heat. Sauté the onion without oil for about a minute to release the liquid. Add the oil and fry until lightly golden.\n" +
                            "Add the ground meat and, using a wooden spoon, crumble the meat. Fry for about 4-5 minutes until the meat changes color.\n" +
                            "Add the spices and celery leaves. Continue frying for 10-15 minutes until the liquid has completely evaporated.\n" +
                            "Transfer the meat to a bowl and wait for it to cool slightly. Wrap in plastic wrap and place in the refrigerator for two and a half to three hours. (You can also put it in the freezer for an hour and a half, stirring occasionally so the meat doesn't freeze)\n" +
                            "We'll transfer to a bowl. We'll put the semolina, salt, oil and water. We'll knead it with our hands for a few minutes. A slightly sticky dough should be obtained. We'll cover it with plastic wrap and set it aside.\n" +
                            "We'll take the meat out of the refrigerator and mix it.\n" +
                            "We'll divide the meat into balls, about 25 grams. Wet each ball and roll it into a ball with our hands.\n" +
                            "We'll arrange it in a baking sheet lined with baking paper and put it in the freezer for about half an hour.\n" +
                            "We'll prepare a small saucer with water.\n" +
                            "After freezing, we'll wet our hands a little with water, take a small piece of the semolina dough and open it with our wrist into a leaf as thin as possible.\n" +
                            "We'll put a meatball in the center and wrap the meat. We'll remove any leftover dough and press well with both hands to remove the air. Wet your hand a little and roll it into a nice circle.\n" +
                            "This is how we roll all the cubes and transfer to the freezer until the soup is ready. The grated beets are boiled with a glass and a half of water.\n" +
                            "In a deep/wide pot on high heat, sauté the onion for about a minute, add the oil and fry until golden brown.\n" +
                            "Add the beets that we cut into cubes and half of the grated beets, fry for 2 minutes, add the celery and stir, add the tomato paste and spices and fry for another 2 minutes.\n" +
                            "Add the water and bring to a boil for 45 minutes.\n" +
                            "Add the grated beets that we left aside and stir.\n" +
                            "Remove the cubes from the freezer, add to the soup and stir\n" +
                            "gently, transfer to medium heat and cook for another 45 minutes.\n" +
                            "Finally, squeeze the juice from half a lemon\n" +
                            "(more is possible according to personal taste)\n" +
                            "\n" +
                            "Enjoy ☺️\n",
                    "drawable/cuba_beet",
                    "cuba_beet_video",
                    "Advanced"
            ));

        lessonDao.insert(new Lesson(
                    UUID.randomUUID().toString(),
                    "Moroccan fish",
                    "Chef Dolev",
                    "Tuna fish in spicy red sauce.",
                    "All you need:\n" +
                            "•\t2 sliced gambas\n" +
                            "•\t3 whole hot peppers, red or green\n" +
                            "•\t4/5 dried red chili pepper\n" +
                            "•\tWhole head of garlic\n" +
                            "•\tBunch of coriander\n" +
                            "•\t1 carrot\n" +
                            "•\t2 potatoes cut into round slices\n" +
                            "•\tCanned chickpeas\n" +
                            "•\t2 tablespoons sweet paprika\n" +
                            "•\t1 and a half tablespoons chicken broth\n" +
                            "•\tHalf a tablespoon salt\n" +
                            "•\tHalf a teaspoon turmeric\n" +
                            "\n" +
                            "Preparation method:\n" +
                            "In a separate large pot, boil 3/4 of a pot of hot water and boil the tuna pieces for 7/8 minutes.\n" +
                            "After boiling under a stream of cold water, clean each piece well to remove all dirt.\n" +
                            "In a separate large pot, arrange all the ingredients except the fish.\n" +
                            "Fry everything with 2 turns of oil for about 5 minutes and mix well.\n" +
                            "Then add the spices and fry them well in the oil.\n" +
                            "Add a cup of hot water and let the sauce cook for about 10 minutes.\n" +
                            "After the sauce is cooked and the potato has softened, arrange the fish pieces in the pot.\n" +
                            "Sprinkle generously with chopped coriander!!! Reduce the heat and close the pot. Let the pot cook for about another fifteen minutes and turn off.\n" +
                            "\n" +
                            "Enjoy ☺️\n",
                    "drawable/moroccan_fish",
                    "moroccan_fish_video",
                    "Advanced"
            ));

        lessonDao.insert(new Lesson(
                    UUID.randomUUID().toString(),
                    "Tricold cake",
                    "Chef Hadar",
                    "Layers of dark, milk and white chocolate.",
                    "An amazing mousse cake made from 3 types of chocolate - dark, milk and white. It is the most delicious and indulgent there is, perfect for birthdays or just to pamper your loved one.\n" +
                            "So let's get to the recipe\n" +
                            "\n" +
                            "Ingredients for the base:\n" +
                            "•\t100 grams flour\n" +
                            "•\t100 grams dark chocolate\n" +
                            "•\t50 grams butter\n" +
                            "•\t2 tablespoons chocolate\n" +
                            "•\t1/2 cup sugar 90 grams\n" +
                            "•\t1/4 teaspoon baking powder\n" +
                            "•\t1/4 cup boiling water\n" +
                            "•\t2 eggs\n" +
                            "•\tA pinch of salt\n" +
                            "\n" +
                            "For dark chocolate mousse:\n" +
                            "•\t150 grams dark chocolate + 100 ml whipping cream 38%\n" +
                            "•\t3 grams gelatin\n" +
                            "•\t15 ml cold water\n" +
                            "•\t200 ml whipping cream 38%\n" +
                            "•\tFor milk chocolate mousse:\n" +
                            "•\t150 grams milk chocolate + 100 ml whipping cream 38%\n" +
                            "•\t3 grams gelatin\n" +
                            "•\t15 ml cold water\n" +
                            "•\t200 ml whipping cream 38%\n" +
                            "\n" +
                            "For white chocolate mousse:\n" +
                            "•\t150 grams white chocolate + 100 ml cream For whipping 38%\n" +
                            "•\t3 grams gelatin\n" +
                            "•\t15 ml cold water\n" +
                            "•\t200 ml whipping cream 38%\n" +
                            "\n" +
                            "For the ganache:\n" +
                            "•\t150 grams milk chocolate\n" +
                            "•\t50 grams dark chocolate\n" +
                            "•\t180 ml whipping cream 38%\n" +
                            "\n" +
                            "How to prepare:\n" +
                            "Preheat oven to 170° turbo\n" +
                            "A 22 diameter ring wraps the bottom in aluminum foil and on top of baking paper, places on the oven tray and sets aside. (You can see in the video)\n" +
                            "Start with the bottom In a small microwave-safe dish, melt the dark chocolate and butter until completely melted.\n" +
                            "In another bowl, lightly beat the eggs, sugar and salt.\n" +
                            "Add the melted chocolate and mix.\n" +
                            "Add the flour, baking powder and chocolate powder, mix only until incorporated.\n" +
                            "Add the boiling water and stir.\n" +
                            "Lightly grease the ring and pour the batter into it. Place in the oven for about 25 minutes or until a toothpick inserted into the cake comes out dry.\n" +
                            "Remove and cool.\n" +
                            "Place a film around the ring so that it is easier to remove the cake and the appearance is cleaner.\n" +
                            "Move to the dark chocolate mousse. In a small saucer, place 3 grams of gelatin in 151 ml of water. Stir lightly and put in the refrigerator for 10-15 minutes to set.\n" +
                            "In a bowl, melt the dark chocolate and 100 ml of cream in the microwave for about a minute and stir until combined.\n" +
                            "Remove the gelatin and put in the microwave for 15 seconds until melted. Add the gelatin to the chocolate and stir.\n" +
                            "Set aside until room temperature. (You can put it in the refrigerator for 5-7 minutes for quick cooling)\n" +
                            "Whip the 200 ml of cream until it has a yogurt texture.\n" +
                            "After the chocolate is at the right temperature, add the chocolate to the cream and mix until it is smooth.\n" +
                            "Pour the cream onto the bottom and freeze for an hour and a half.\n" +
                            "Repeat with a whisk from W Rocherold\n" +
                            "Just until incorporated.\n" +
                            "Add the boiling water and mix.\n" +
                            "Lightly grease the ring and pour the batter in. Put in the oven for about 25 minutes or until a toothpick inserted into the cake comes out dry.\n" +
                            "Remove and cool.\n" +
                            "Place a film around the ring so it is easier to extract the cake and the appearance is cleaner.\n" +
                            "Move to the dark chocolate mousse in a small saucer. Place 3 grams of gelatin in 151 ml of water, mix lightly and put in the refrigerator for 10-15 minutes to set.\n" +
                            "In a bowl, melt the dark chocolate and 100 ml of cream in the microwave for about a minute and mix until combined.\n" +
                            "Remove the gelatin and put it in the microwave for 15 seconds until melted.\n" +
                            "Add the gelatin to the chocolate and mix.\n" +
                            "Set aside until room temperature. (You can put it in the refrigerator for 5-7 minutes for quick cooling)\n" +
                            "Whip the 200 ml of cream until it has a yogurt consistency.\n" +
                            "After the chocolate is at the right temperature, add the chocolate to the cream and mix until it is smooth.\n" +
                            "Pour the cream on top of the base and freeze for an hour and a half.\n" +
                            "Repeat the process with the other types of chocolate.\n" +
                            "First the milk chocolate and then the white chocolate.\n" +
                            "Be careful not to skip freezing between layers!\n" +
                            "For the last layer, transfer to the freezer overnight and the next day, melt the ganache ingredients together in a microwave, stir, wait a little and pour over the cake.\n" +
                            "Put back in the freezer for at least an hour.\n" +
                            "Remove the cake from the herring and foil and decorate with whatever you like.\n" +
                            "\n" +
                            "Enjoy ☺️\n",
                    "drawable/tricold_cake",
                    "tricold_cake_video",
                    "Advanced"
            ));
        }
    }
