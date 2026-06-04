package com.tunnellight.biblepromise;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A bundled, offline collection of uplifting passages from the World English
 * Bible (WEB, public domain), organised by topic (the topical grouping powers
 * the Browse screen, while a flattened view powers the daily verse and the
 * shuffle button).
 */
final class VerseRepository {

    private static final List<Topic> TOPICS = buildTopics();
    /** Every verse, in topic order; index here is the stable "global" id. */
    private static final List<Verse> ALL = flatten();

    private final Random random = new Random();

    // ---- Topical access (Browse screen) ------------------------------------

    List<Topic> topics() {
        return TOPICS;
    }

    /** The global id of the child verse at {@code (group, child)}. */
    int globalIndex(int group, int child) {
        int index = 0;
        for (int g = 0; g < group; g++) {
            index += TOPICS.get(g).verses.size();
        }
        return index + child;
    }

    // ---- Flat access (daily verse + shuffle) -------------------------------

    int size() {
        return ALL.size();
    }

    Verse get(int index) {
        return ALL.get(index);
    }

    /**
     * The verse for a given day. Stable for the whole calendar day and across
     * app launches, advancing by one each day.
     */
    int indexForDate(LocalDate date) {
        long ordinal = date.toEpochDay();
        int mod = (int) (ordinal % ALL.size());
        return mod < 0 ? mod + ALL.size() : mod;
    }

    /** A random index different from {@code currentIndex} (when possible). */
    int randomIndexExcluding(int currentIndex) {
        if (ALL.size() <= 1) {
            return 0;
        }
        int next;
        do {
            next = random.nextInt(ALL.size());
        } while (next == currentIndex);
        return next;
    }

    // ---- Data --------------------------------------------------------------

    private static List<Verse> flatten() {
        List<Verse> all = new ArrayList<>();
        for (Topic topic : TOPICS) {
            all.addAll(topic.verses);
        }
        return Collections.unmodifiableList(all);
    }

    private static Topic topic(String name, Verse... verses) {
        return new Topic(name, Collections.unmodifiableList(Arrays.asList(verses)));
    }

    private static List<Topic> buildTopics() {
        return Collections.unmodifiableList(Arrays.asList(

                topic("Hope",
                        new Verse("For I know the thoughts that I think toward you, says the LORD, thoughts of peace, and not of evil, to give you hope and a future.",
                                "Jeremiah 29:11"),
                        new Verse("Now may the God of hope fill you with all joy and peace in believing, that you may abound in hope, in the power of the Holy Spirit.",
                                "Romans 15:13"),
                        new Verse("It is because of the LORD’s loving kindnesses that we are not consumed, because his compassion doesn’t fail. They are new every morning. Great is your faithfulness.",
                                "Lamentations 3:22-23"),
                        new Verse("Now faith is assurance of things hoped for, proof of things not seen.",
                                "Hebrews 11:1"),
                        new Verse("We know that all things work together for good for those who love God, for those who are called according to his purpose.",
                                "Romans 8:28")),

                topic("Anxiety & Worry",
                        new Verse("In nothing be anxious, but in everything, by prayer and petition with thanksgiving, let your requests be made known to God. And the peace of God, which surpasses all understanding, will guard your hearts and your thoughts in Christ Jesus.",
                                "Philippians 4:6-7"),
                        new Verse("casting all your worries on him, because he cares for you.",
                                "1 Peter 5:7"),
                        new Verse("Therefore don’t be anxious for tomorrow, for tomorrow will be anxious for itself. Each day’s own evil is sufficient.",
                                "Matthew 6:34"),
                        new Verse("In the multitude of my thoughts within me, your comforts delight my soul.",
                                "Psalm 94:19"),
                        new Verse("You will keep whoever’s mind is steadfast in perfect peace, because he trusts in you.",
                                "Isaiah 26:3")),

                topic("Fear",
                        new Verse("Don’t you be afraid, for I am with you. Don’t be dismayed, for I am your God. I will strengthen you. Yes, I will help you. Yes, I will uphold you with the right hand of my righteousness.",
                                "Isaiah 41:10"),
                        new Verse("Haven’t I commanded you? Be strong and courageous. Don’t be afraid. Don’t be dismayed, for the LORD your God is with you wherever you go.",
                                "Joshua 1:9"),
                        new Verse("The LORD is my light and my salvation. Whom shall I fear? The LORD is the strength of my life. Of whom shall I be afraid?",
                                "Psalm 27:1"),
                        new Verse("For God didn’t give us a spirit of fear, but of power, love, and self-control.",
                                "2 Timothy 1:7"),
                        new Verse("When I am afraid, I will put my trust in you.",
                                "Psalm 56:3")),

                topic("Depression",
                        new Verse("The LORD is near to those who have a broken heart, and saves those who have a crushed spirit.",
                                "Psalm 34:18"),
                        new Verse("Why are you in despair, my soul? Why are you disturbed within me? Hope in God! For I shall still praise him, the saving help of my countenance, and my God.",
                                "Psalm 42:11"),
                        new Verse("I waited patiently for the LORD. He turned to me, and heard my cry. He brought me up also out of a horrible pit, out of the miry clay. He set my feet on a rock, and gave me a firm place to stand.",
                                "Psalm 40:1-2"),
                        new Verse("Weeping may stay for the night, but joy comes in the morning.",
                                "Psalm 30:5"),
                        new Verse("to provide for those who mourn in Zion, to give to them a garland for ashes, the oil of joy for mourning, the garment of praise for the spirit of heaviness.",
                                "Isaiah 61:3")),

                topic("Addiction & Freedom",
                        new Verse("No temptation has taken you except what is common to man. God is faithful, who will not allow you to be tempted above what you are able, but will with the temptation also make the way of escape, that you may be able to endure it.",
                                "1 Corinthians 10:13"),
                        new Verse("If therefore the Son makes you free, you will be free indeed.",
                                "John 8:36"),
                        new Verse("Stand firm therefore in the liberty by which Christ has made us free, and don’t be entangled again with a yoke of bondage.",
                                "Galatians 5:1"),
                        new Verse("For sin will not have dominion over you, for you are not under law, but under grace.",
                                "Romans 6:14"),
                        new Verse("Therefore if anyone is in Christ, he is a new creation. The old things have passed away. Behold, all things have become new.",
                                "2 Corinthians 5:17")),

                topic("Strength",
                        new Verse("I can do all things through Christ, who strengthens me.",
                                "Philippians 4:13"),
                        new Verse("but those who wait for the LORD will renew their strength. They will mount up with wings like eagles. They will run, and not be weary. They will walk, and not faint.",
                                "Isaiah 40:31"),
                        new Verse("The LORD is my strength and my shield. My heart has trusted in him, and I am helped. Therefore my heart greatly rejoices. With my song I will thank him.",
                                "Psalm 28:7"),
                        new Verse("He has said to me, “My grace is sufficient for you, for my power is made perfect in weakness.” Most gladly therefore I will rather glory in my weaknesses, that the power of Christ may rest on me.",
                                "2 Corinthians 12:9"),
                        new Verse("...for the joy of the LORD is your strength.",
                                "Nehemiah 8:10")),

                topic("Peace",
                        new Verse("Peace I leave with you. My peace I give to you; not as the world gives, I give to you. Don’t let your heart be troubled, neither let it be fearful.",
                                "John 14:27"),
                        new Verse("The LORD bless you, and keep you. The LORD make his face to shine on you, and be gracious to you. The LORD lift up his face toward you, and give you peace.",
                                "Numbers 6:24-26"),
                        new Verse("And let the peace of God rule in your hearts, to which also you were called in one body, and be thankful.",
                                "Colossians 3:15"),
                        new Verse("In peace I will both lay myself down and sleep, for you, LORD alone, make me live in safety.",
                                "Psalm 4:8"),
                        new Verse("Be still, and know that I am God. I will be exalted among the nations. I will be exalted in the earth.",
                                "Psalm 46:10")),

                topic("Comfort & Grief",
                        new Verse("Even though I walk through the valley of the shadow of death, I will fear no evil, for you are with me. Your rod and your staff, they comfort me.",
                                "Psalm 23:4"),
                        new Verse("Blessed are those who mourn, for they shall be comforted.",
                                "Matthew 5:4"),
                        new Verse("Blessed be the God and Father of our Lord Jesus Christ, the Father of mercies and God of all comfort, who comforts us in all our affliction.",
                                "2 Corinthians 1:3-4"),
                        new Verse("He will wipe away every tear from their eyes. Death will be no more; neither will there be mourning, nor crying, nor pain any more. The first things have passed away.",
                                "Revelation 21:4"),
                        new Verse("Come to me, all you who labor and are heavily burdened, and I will give you rest.",
                                "Matthew 11:28")),

                topic("Loneliness",
                        new Verse("...for he has said, “I will in no way leave you, neither will I in any way forsake you.”",
                                "Hebrews 13:5"),
                        new Verse("Where could I go from your Spirit? Or where could I flee from your presence? ...even there your hand will lead me, and your right hand will hold me.",
                                "Psalm 139:7-10"),
                        new Verse("...Behold, I am with you always, even to the end of the age.",
                                "Matthew 28:20"),
                        new Verse("Be strong and courageous. Don’t be afraid or scared of them; for the LORD your God himself is who goes with you. He will not fail you nor forsake you.",
                                "Deuteronomy 31:6")),

                topic("Guidance",
                        new Verse("Trust in the LORD with all your heart, and don’t lean on your own understanding. In all your ways acknowledge him, and he will make your paths straight.",
                                "Proverbs 3:5-6"),
                        new Verse("I will instruct you and teach you in the way which you shall go. I will counsel you with my eye on you.",
                                "Psalm 32:8"),
                        new Verse("Your word is a lamp to my feet, and a light for my path.",
                                "Psalm 119:105"),
                        new Verse("and when you turn to the right hand, and when you turn to the left, your ears will hear a voice behind you, saying, “This is the way. Walk in it.”",
                                "Isaiah 30:21"),
                        new Verse("But if any of you lacks wisdom, let him ask of God, who gives to all liberally and without reproach, and it will be given to him.",
                                "James 1:5")),

                topic("Faith",
                        new Verse("for we walk by faith, not by sight.",
                                "2 Corinthians 5:7"),
                        new Verse("Therefore I tell you, all things whatever you pray and ask for, believe that you have received them, and you shall have them.",
                                "Mark 11:24"),
                        new Verse("...for most certainly I tell you, if you have faith as a grain of mustard seed, you will tell this mountain, ‘Move from here to there,’ and it will move; and nothing will be impossible for you.",
                                "Matthew 17:20"),
                        new Verse("for by grace you have been saved through faith, and that not of yourselves; it is the gift of God.",
                                "Ephesians 2:8")),

                topic("Forgiveness",
                        new Verse("If we confess our sins, he is faithful and righteous to forgive us the sins, and to cleanse us from all unrighteousness.",
                                "1 John 1:9"),
                        new Verse("And be kind to one another, tender hearted, forgiving each other, just as God also in Christ forgave you.",
                                "Ephesians 4:32"),
                        new Verse("bearing with one another, and forgiving each other, if any man has a complaint against any; even as Christ forgave you, so you also do.",
                                "Colossians 3:13"),
                        new Verse("As far as the east is from the west, so far has he removed our transgressions from us.",
                                "Psalm 103:12"),
                        new Verse("He will again have compassion on us. He will tread our iniquities under foot; and you will cast all their sins into the depths of the sea.",
                                "Micah 7:19")),

                topic("Gratitude",
                        new Verse("Always rejoice. Pray without ceasing. In everything give thanks, for this is the will of God in Christ Jesus toward you.",
                                "1 Thessalonians 5:16-18"),
                        new Verse("Give thanks to the LORD, for he is good, for his loving kindness endures forever.",
                                "Psalm 107:1"),
                        new Verse("This is the day that the LORD has made. We will rejoice and be glad in it!",
                                "Psalm 118:24")),

                topic("Love",
                        new Verse("For God so loved the world, that he gave his one and only Son, that whoever believes in him should not perish, but have eternal life.",
                                "John 3:16"),
                        new Verse("For I am persuaded that neither death, nor life, nor angels, nor principalities, nor things present, nor things to come, nor powers, nor height, nor depth, nor any other created thing will be able to separate us from God’s love which is in Christ Jesus our Lord.",
                                "Romans 8:38-39"),
                        new Verse("Love is patient and is kind. Love doesn’t envy. Love doesn’t brag, is not proud, doesn’t behave itself inappropriately, doesn’t seek its own way, is not provoked, takes no account of evil.",
                                "1 Corinthians 13:4-5"),
                        new Verse("There is no fear in love; but perfect love casts out fear, because fear has punishment. He who fears is not made perfect in love.",
                                "1 John 4:18"),
                        new Verse("The LORD, your God, is among you, a mighty one who will save. He will rejoice over you with joy. He will calm you in his love. He will rejoice over you with singing.",
                                "Zephaniah 3:17")),

                topic("Healing",
                        new Verse("Heal me, O LORD, and I will be healed. Save me, and I will be saved; for you are my praise.",
                                "Jeremiah 17:14"),
                        new Verse("He heals the broken in heart, and binds up their wounds.",
                                "Psalm 147:3"),
                        new Verse("...by whose stripes you were healed.",
                                "1 Peter 2:24"),
                        new Verse("Praise the LORD, my soul, and don’t forget all his benefits, who forgives all your sins, who heals all your diseases,",
                                "Psalm 103:2-3"),
                        new Verse("and the prayer of faith will heal him who is sick, and the Lord will raise him up. If he has committed sins, he will be forgiven.",
                                "James 5:15")),

                topic("Trust",
                        new Verse("The LORD is my shepherd; I shall lack nothing.",
                                "Psalm 23:1"),
                        new Verse("I have set the LORD always before me. Because he is at my right hand, I shall not be moved.",
                                "Psalm 16:8"),
                        new Verse("Also delight yourself in the LORD, and he will give you the desires of your heart.",
                                "Psalm 37:4"),
                        new Verse("I will lift up my eyes to the hills. Where does my help come from? My help comes from the LORD, who made heaven and earth.",
                                "Psalm 121:1-2"),
                        new Verse("The name of the LORD is a strong tower: the righteous run to him, and are safe.",
                                "Proverbs 18:10"),
                        new Verse("My flesh and my heart fails, but God is the strength of my heart and my portion forever.",
                                "Psalm 73:26"),
                        new Verse("Oh taste and see that the LORD is good. Blessed is the man who takes refuge in him.",
                                "Psalm 34:8"))
        ));
    }
}
