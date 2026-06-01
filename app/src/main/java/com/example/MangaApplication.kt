package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ChapterEntity
import com.example.data.MangaEntity
import com.example.data.MangaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MangaApplication : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MangaRepository(database.mangaDao()) }

    override fun onCreate() {
        super.onCreate()
        
        // Seed database in background scope on launch if empty
        CoroutineScope(Dispatchers.IO).launch {
            val dao = database.mangaDao()
            // Check if db is already seeded
            val existing = dao.getChaptersForManga("demon_slayer")
            if (existing.isEmpty()) {
                seedDatabase()
            }
        }
    }

    private suspend fun seedDatabase() {
        val dao = database.mangaDao()

        // 1. Seed Manga Entries
        val mangas = listOf(
            MangaEntity(
                id = "demon_slayer",
                titleAr = "قاتل الشياطين",
                titleEn = "Demon Slayer",
                author = "Koyoharu Gotouge",
                descriptionAr = "في عهد تايشو باليابان، يجد الفتى الطيب تانجيرو كامادو عائلته مذبوحة على يد شيطان. الناجية الوحيدة هي أخته الصغيرة نيزوكو، لكنها تحولت لشيطان هي الأخرى. يتعهد تانجيرو بالانضمام لفيلق قتلة الشياطين لإعادة أخته إلى طبيعتها البشرية والانتقام لعائلته.",
                coverGradientStart = 0xFF6366F1, // Indigo
                coverGradientEnd = 0xFFA855F7,   // Purple
                status = "مكتمل",
                rating = 4.9f,
                genres = "أكشن, خيال, شونين, دراما",
                sourceName = "مانجا ليك (MangaLek)",
                isBookmarked = true,
                ratingVotes = 2480
            ),
            MangaEntity(
                id = "one_piece",
                titleAr = "ون بيس",
                titleEn = "One Piece",
                author = "Eiichiro Oda",
                descriptionAr = "المغامرة الكبرى للحصول على الكنز الأسطوري 'ون بيس' الذي تركه ملك القراصنة جول دي روجر. مونكي دي لوفي، الفتى الذي اكتسب قوى مطاطية بعد تناول فاكهة الشيطان، يبحر مع طاقمه الخاص للبحث عن الكنز ليصبح ملك القراصنة الجديد.",
                coverGradientStart = 0xFF0EA5E9, // Teal / Cyan Light
                coverGradientEnd = 0xFF2563EB,   // Ocean Royal Blue
                status = "مستمر",
                rating = 4.8f,
                genres = "مغامرة, أكشن, كوميديا, خيال",
                sourceName = "مانجا ديكس (MangaDex)",
                isBookmarked = false,
                ratingVotes = 4210
            ),
            MangaEntity(
                id = "attack_on_titan",
                titleAr = "هجوم العمالقة",
                titleEn = "Attack on Titan",
                author = "Hajime Isayama",
                descriptionAr = "منذ قرون، تعرضت البشرية للإبادة على يد وحوش عملاقة تُدعى العمالقة. يعيش الناجون خلف أسوار هائلة لحماية أنفسهم. يتغير كل شيء عندما يظهر عملاق ضخم ويخترق السور الخارجي، مغيراً حياة إيرين ييغر الذي يقسم على إبادة كل عملاق.",
                coverGradientStart = 0xFFEF4444, // Blood Red
                coverGradientEnd = 0xFF1E1B4B,   // Dark Indigo Dark Slate
                status = "مكتمل",
                rating = 4.9f,
                genres = "غموض, أكشن, مأساوي, عسكري",
                sourceName = "MangaSlayer",
                isBookmarked = false,
                ratingVotes = 3105
            ),
            MangaEntity(
                id = "solo_leveling",
                titleAr = "نهوض المستوى الفردي",
                titleEn = "Solo Leveling",
                author = "Chugong",
                descriptionAr = "في عالم ظهرت فيه بوابات تربطه ببراري الوحوش اللعينة، يُمنح أشخاص عاديون قوى لصيد الوحوش يُدعون بالصيادين. سونغ جين وو هو صياد ضعيف برتبة E، لكن بعد نجاته من زنزانة مزدوجة غامضة ومميتة، يكتسب قدرة فريدة تسمى 'النظام' تتيح له الارتقاء بقوته بلا حدود.",
                coverGradientStart = 0xFF06B6D4, // Electric Blue
                coverGradientEnd = 0xFF0F172A,   // Dark Slate
                status = "مكتمل",
                rating = 4.7f,
                genres = "أكشن, قوة بالغة, بوابة, خيال",
                sourceName = "بوابة المانجا",
                isBookmarked = true,
                ratingVotes = 1950
            ),
            MangaEntity(
                id = "jujutsu_kaisen",
                titleAr = "سحر مجابهة اللعنات",
                titleEn = "Jujutsu Kaisen",
                author = "Gege Akutami",
                descriptionAr = "يتغذى الشياطين على البشر الضعفاء الغافلين، وتضيع أجزاء من الشيطان الأسطوري ريومن سوكونا وتتفرق. صبي المدرسة الثانوية يوجي إيتادوري يبتلع إحدى هذه الأجزاء اللعينة بدافع الاضطرار لحماية أصدقائه، لينخرط في مدرسة السحر لمواجهة اللعنات وقتال قوى الشر.",
                coverGradientStart = 0xFF10B981, // Emerald Green
                coverGradientEnd = 0xFF111827,   // Carbon Black
                status = "مستمر",
                rating = 4.6f,
                genres = "خارق للطبيعة, رعب, شونين, أكشن",
                sourceName = "مانجا ليك (MangaLek)",
                isBookmarked = false,
                ratingVotes = 1530
            )
        )
        dao.insertMangas(mangas)

        // 2. Seed Chapter Entries
        val chapters = mutableListOf<ChapterEntity>()
        mangas.forEach { manga ->
            val count = when (manga.id) {
                "one_piece" -> 8
                "solo_leveling" -> 7
                "demon_slayer" -> 6
                else -> 5
            }
            for (i in 1..count) {
                chapters.add(
                    ChapterEntity(
                        id = "${manga.id}_ch_$i",
                        mangaId = manga.id,
                        title = "الفصل $i: ${getChapterArabicTitle(manga.id, i)}",
                        number = i.toDouble(),
                        releaseDate = "2026/05/${10 + i}",
                        isRead = false,
                        lastReadPage = 0,
                        totalPages = 10 + (i % 3) * 2 // Chapters have between 10, 12, or 14 stylized pages
                    )
                )
            }
        }
        dao.insertChapters(chapters)
    }

    private fun getChapterArabicTitle(mangaId: String, index: Int): String {
        return when (mangaId) {
            "demon_slayer" -> when (index) {
                1 -> "القسوة والغضب"
                2 -> "الغريب المجهول"
                3 -> "العودة للوطن"
                4 -> "مدرب جبل ساغيري"
                5 -> "اختبار القبول النهائي"
                6 -> "القتال الأول"
                else -> "فجر البداية"
            }
            "one_piece" -> when (index) {
                1 -> "رومانس داون"
                2 -> "الفتى ذو قبعة القش"
                3 -> "ظهور صائد القراصنة زورو"
                4 -> "ابن البحر كول"
                5 -> "ملك قراصنة المستقبل"
                6 -> "الفتاة اللّصة نامي"
                7 -> "قتال في مدينة البرتقال"
                8 -> "طريق باتجاه الجراند لاين"
                else -> "الوجهة الأسطورية"
            }
            "attack_on_titan" -> when (index) {
                1 -> "إليك، بعد ألفي عام من الآن"
                2 -> "ذلك اليوم العصيب"
                3 -> "سقوط ماريا"
                4 -> "بصيص أمل وسط اليأس"
                5 -> "تخرج الفيلق الـ 104"
                else -> "هجوم مضاد"
            }
            "solo_leveling" -> when (index) {
                1 -> "أضعف صياد للبشرية"
                2 -> "المعبد الغامض المزدوج"
                3 -> "القوانين الثلاثة العظيمة"
                4 -> "الموت أو جني المكاسب"
                5 -> "تحديث اللعبة الاستثنائية"
                6 -> "الارتقاء للمستوى المنفرد"
                7 -> "الوحوش رتبة D"
                else -> "نهوض جين وو"
            }
            "jujutsu_kaisen" -> when (index) {
                1 -> "الوحش ذو الإصبع الملعون"
                2 -> "الإعدام السري المؤجل"
                3 -> "من أجل نبل روحي"
                4 -> "فتاة الفولاذ ون شيتو"
                5 -> "الرحم الملعون المروّع"
                else -> "القتال اللانهائي"
            }
            else -> "فصل جديد مشوق"
        }
    }
}
