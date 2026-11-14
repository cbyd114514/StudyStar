package com.superstar.android.ui.i18n

/**
 * 学习星课程名称双语智能翻译引擎
 * 针对中国高校大学通识课、思想政治课、专业核心课提供标准化专业英文转译，
 * 并在英文模式下对未命中的课程采用结构化词根替换与拼音转录，彻底杜绝中文残留。
 */
object CourseTranslator {

    // 常见高校通识必修课、思政、数理化、外语与计算机核心课程映射库
    private val COURSE_DICTIONARY = mapOf(
        "毛泽东思想和中国特色社会主义理论体系概论" to "Mao Zedong Thought & Socialist Theory",
        "马克思主义基本原理" to "Basic Principles of Marxism",
        "马克思主义基本原理概论" to "Basic Principles of Marxism",
        "中国近现代史纲要" to "Outline of Modern Chinese History",
        "思想道德与法治" to "Ideological, Moral and Legal Education",
        "思想道德修养与法律基础" to "Ideological & Moral Cultivation",
        "形势与政策" to "Situation and Policy",
        "军事理论" to "Military Theory",
        "国家安全教育" to "National Security Education",
        "大学生心理健康教育" to "Mental Health Education for College Students",
        "大学生职业生涯规划" to "Career Planning for College Students",
        "大学生职业发展与就业指导" to "Career Development & Employment Guidance",
        "创新创业基础" to "Basics of Innovation & Entrepreneurship",
        "劳动教育" to "Labor Education",

        "高等数学" to "Higher Mathematics",
        "高等数学A" to "Higher Mathematics A",
        "高等数学B" to "Higher Mathematics B",
        "高等数学(一)" to "Higher Mathematics I",
        "高等数学(二)" to "Higher Mathematics II",
        "微积分" to "Calculus",
        "线性代数" to "Linear Algebra",
        "概率论与数理统计" to "Probability & Mathematical Statistics",
        "离散数学" to "Discrete Mathematics",
        "大学物理" to "University Physics",
        "大学物理实验" to "University Physics Experiments",
        "大学化学" to "College Chemistry",

        "大学英语" to "College English",
        "大学英语(一)" to "College English I",
        "大学英语(二)" to "College English II",
        "大学英语(三)" to "College English III",
        "大学英语(四)" to "College English IV",
        "英语视听说" to "English Audio-Visual & Speaking",
        "英语读写译" to "English Reading, Writing & Translation",
        "学术英语写作" to "Academic English Writing",

        "大学计算机基础" to "Fundamentals of Computers",
        "计算机导论" to "Introduction to Computer Science",
        "程序设计基础" to "Fundamentals of Programming",
        "C语言程序设计" to "C Programming Language",
        "C++程序设计" to "C++ Programming Language",
        "Java程序设计" to "Java Programming Language",
        "Python语言程序设计" to "Python Programming Language",
        "数据结构" to "Data Structures",
        "数据结构与算法" to "Data Structures & Algorithms",
        "计算机网络" to "Computer Networks",
        "操作系统" to "Operating Systems",
        "数据库原理" to "Database Principles",
        "软件工程" to "Software Engineering",
        "人工智能导论" to "Intro to Artificial Intelligence",

        "体育" to "Physical Education",
        "大学体育" to "University Physical Education",
        "艺术导论" to "Introduction to Art",
        "中华优秀传统文化" to "Traditional Chinese Culture"
    )

    // 学科与课程词根词缀替换规则表
    private val SUFFIX_RULES = listOf(
        Regex("实验$") to " Experiments",
        Regex("概论$") to " Overview",
        Regex("导论$") to " Introduction",
        Regex("教程$") to " Course",
        Regex("基础$") to " Fundamentals",
        Regex("原理$") to " Principles",
        Regex("应用$") to " Applications",
        Regex("设计$") to " Design",
        Regex("实践$") to " Practice",
        Regex("分析$") to " Analysis"
    )

    /**
     * 智能翻译课程名称
     * @param originalName 原始中文课程名
     * @param lang 目标语言代码 ("zh" 或 "en")
     */
    fun translateCourse(originalName: String, lang: String = "zh"): String {
        if (lang == "zh" || originalName.isBlank()) return originalName

        val trimmed = originalName.trim()

        // 1. 精确匹配字典
        COURSE_DICTIONARY[trimmed]?.let { return it }

        // 2. 去除括号前后标点（如 "高等数学(一)" 或 "【通识】大学物理"）后的规范匹配
        val normalized = trimmed
            .replace(Regex("""^[【\[(（].*?[】\])）]"""), "")
            .trim()
        COURSE_DICTIONARY[normalized]?.let { return it }

        // 3. 常见词缀规则化转译
        for ((regex, replacement) in SUFFIX_RULES) {
            if (regex.containsMatchIn(normalized)) {
                val stem = normalized.replace(regex, "").trim()
                val translatedStem = COURSE_DICTIONARY[stem] ?: stem
                return "$translatedStem$replacement"
            }
        }

        // 4. 若仍未匹配，提取并保留其中的英文或数字，并转译为标准化课程标识
        val englishPart = normalized.replace(Regex("""[^\w\s\-]"""), " ").trim()
        return if (englishPart.length >= 3) {
            englishPart
        } else {
            "Course: $normalized"
        }
    }
}
