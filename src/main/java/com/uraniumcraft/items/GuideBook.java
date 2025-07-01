package com.uraniumcraft.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;
import java.util.List;

public class GuideBook {
    
    public static ItemStack createGuideBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        
        meta.setTitle(ChatColor.GOLD + "UraniumCraft - Руководство");
        meta.setAuthor(ChatColor.AQUA + "UraniumCraft Team");
        meta.setDisplayName(ChatColor.GOLD + "📖 Руководство UraniumCraft");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Полное руководство по плагину",
            ChatColor.YELLOW + "Содержит все инструкции и рецепты",
            ChatColor.GREEN + "Версия 2.0.0"
        ));
        
        // Добавляем страницы
        meta.setPages(createBookPages());
        
        book.setItemMeta(meta);
        return book;
    }
    
    private static List<String> createBookPages() {
        return Arrays.asList(
            // Страница 1 - Титульная
            ChatColor.GOLD + "" + ChatColor.BOLD + "UraniumCraft\n" +
            ChatColor.GOLD + "" + ChatColor.BOLD + "Руководство\n\n" +
            ChatColor.DARK_BLUE + "Версия 2.0.0\n\n" +
            ChatColor.BLACK + "Добро пожаловать в мир урановых технологий!\n\n" +
            ChatColor.GRAY + "Это руководство содержит всю необходимую информацию для работы с плагином.\n\n" +
            ChatColor.DARK_GRAY + "© UraniumCraft Team",
            
            // Страница 2 - Содержание
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Содержание:\n\n" +
            ChatColor.BLACK + "1. Основы\n" +
            "2. Урановые предметы\n" +
            "3. Система радиации\n" +
            "4. Центрифуга\n" +
            "5. Лаборатория\n" +
            "6. Исследования\n" +
            "7. Продвинутые предметы\n" +
            "8. Команды\n" +
            "9. Рецепты\n" +
            "10. Советы",
            
            // Страница 3 - Основы
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "1. Основы\n\n" +
            ChatColor.BLACK + "UraniumCraft добавляет:\n" +
            "• Урановые предметы\n" +
            "• Систему радиации\n" +
            "• Центрифугу\n" +
            "• Лаборатории\n" +
            "• Продвинутые предметы\n\n" +
            ChatColor.DARK_RED + "⚠ Внимание!\n" +
            ChatColor.BLACK + "Урановые предметы радиоактивны и могут нанести вред!",
            
            // Страница 4 - Урановые предметы
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "2. Урановые предметы\n\n" +
            ChatColor.BLACK + "• " + ChatColor.GREEN + "Урановая руда" + ChatColor.BLACK + " - базовый материал\n" +
            "• " + ChatColor.GREEN + "Урановый слиток" + ChatColor.BLACK + " - переработанная руда\n" +
            "• " + ChatColor.AQUA + "Обогащённый уран" + ChatColor.BLACK + " - для топлива\n" +
            "• " + ChatColor.YELLOW + "Топливный стержень" + ChatColor.BLACK + " - источник энергии\n" +
            "• " + ChatColor.GRAY + "Обеднённый уран" + ChatColor.BLACK + " - отходы\n" +
            "• " + ChatColor.DARK_GREEN + "Урановая пыль" + ChatColor.BLACK + " - для крафта",
            
            // Страница 5 - Система радиации
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "3. Система радиации\n\n" +
            ChatColor.BLACK + "Урановые предметы излучают радиацию:\n\n" +
            "• Урановая пыль: 8 ед.\n" +
            "• Урановый слиток: 10 ед.\n" +
            "• Обогащённый уран: 25 ед.\n" +
            "• Топливный стержень: 50 ед.\n\n" +
            ChatColor.DARK_RED + "Эффекты радиации:\n" +
            ChatColor.BLACK + "Тошнота, слабость, урон",
            
            // Страница 6 - Защита от радиации
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Защита от радиации\n\n" +
            ChatColor.BLACK + "Броня защищает от радиации:\n\n" +
            "• Кожаная: 10%\n" +
            "• Железная: 25%\n" +
            "• Алмазная: 40%\n" +
            "• Незеритовая: 60%\n" +
            "• " + ChatColor.YELLOW + "Костюм химзащиты: 100%\n" +
            "• " + ChatColor.AQUA + "Силовая броня: 95%\n\n" +
            ChatColor.WHITE + "Таблетки от радиации" + ChatColor.BLACK + " снижают уровень на 20 единиц.",
            
            // Страница 7 - Центрифуга
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "4. Центрифуга\n\n" +
            ChatColor.BLACK + "Постройте ромб из 4 котлов с водой в области 3x3:\n\n" +
            "  К\n" +
            "К Ц К\n" +
            "  К\n\n" +
            "К = Котёл с водой\n" +
            "Ц = Центр\n\n" +
            ChatColor.DARK_GREEN + "Команды:\n" +
            ChatColor.BLACK + "/centrifuge create\n" +
            "/centrifuge start\n" +
            "/centrifuge status",
            
            // Страница 8 - Процесс центрифугирования
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Центрифугирование\n\n" +
            ChatColor.BLACK + "1. Постройте структуру\n" +
            "2. Наполните котлы водой\n" +
            "3. Встаньте в центр\n" +
            "4. /centrifuge create\n" +
            "5. /centrifuge start\n\n" +
            ChatColor.DARK_GREEN + "Результат:\n" +
            ChatColor.BLACK + "Через 5 минут получите урановую пыль!\n\n" +
            ChatColor.GRAY + "Вода исчезнет после процесса.",
            
            // Страница 9 - Лаборатория
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "5. Лаборатория\n\n" +
            ChatColor.BLACK + "Для создания лаборатории нужна авторизация администратора.\n\n" +
            ChatColor.DARK_GREEN + "Команды админа:\n" +
            ChatColor.BLACK + "/laboratory authorize <игрок>\n" +
            "/laboratory unauthorize <игрок>\n\n" +
            ChatColor.DARK_GREEN + "Создание:\n" +
            ChatColor.BLACK + "1. Получите блок лаборатории\n" +
            "2. Поставьте его\n" +
            "3. Приносите материалы",
            
            // Страница 10 - Материалы для лаборатории
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Материалы для лаборатории\n\n" +
            ChatColor.BLACK + "Необходимо:\n" +
            "• Железные блоки: 64\n" +
            "• Редстоун блоки: 32\n" +
            "• Алмазные блоки: 16\n" +
            "• Изумрудные блоки: 8\n" +
            "• Маяки: 4\n" +
            "• Звёзды Нижнего мира: 2\n\n" +
            ChatColor.GRAY + "Просто держите материалы в руке и кликните по блоку лаборатории.",
            
            // Страница 11 - Исследования
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "6. Исследования\n\n" +
            ChatColor.BLACK + "В лаборатории можно изучать:\n\n" +
            "• " + ChatColor.YELLOW + "Костюм химзащиты" + ChatColor.BLACK + " (20 мин)\n" +
            "• " + ChatColor.GREEN + "Урановая капсула" + ChatColor.BLACK + " (15 мин)\n" +
            "• " + ChatColor.AQUA + "Силовая броня" + ChatColor.BLACK + " (30 мин)\n" +
            "• " + ChatColor.GOLD + "Автошахтёр" + ChatColor.BLACK + " (40 мин)\n" +
            "• " + ChatColor.RED + "Рельсотрон" + ChatColor.BLACK + " (45 мин)\n" +
            "• " + ChatColor.BLUE + "Электротранспорт" + ChatColor.BLACK + " (60 мин)",
            
            // Страница 12 - Продвинутые предметы
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "7. Продвинутые предметы\n\n" +
            ChatColor.AQUA + "Силовая броня:\n" +
            ChatColor.BLACK + "Режимы: Стандартный, Защита, Скорость, Прыжки\n\n" +
            ChatColor.RED + "Рельсотрон:\n" +
            ChatColor.BLACK + "Режимы: Одиночный, Очередь, Пробивной\n\n" +
            ChatColor.BLUE + "Электротранспорт:\n" +
            ChatColor.BLACK + "Режимы: Стандартный, Турбо, Эко",
            
            // Страница 13 - Продвинутые предметы (продолжение)
            ChatColor.GOLD + "Автошахтёр:\n" +
            ChatColor.BLACK + "Режимы: Обычная добыча, Только руды, Глубокая добыча\n\n" +
            ChatColor.GREEN + "Урановая капсула:\n" +
            ChatColor.BLACK + "Безопасное хранение урана (64 единицы)\n" +
            "ПКМ - поместить\n" +
            "Shift+ПКМ - извлечь\n\n" +
            ChatColor.GRAY + "Все предметы переключают режимы по ПКМ",
            
            // Страница 14 - Команды
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "8. Команды\n\n" +
            ChatColor.DARK_GREEN + "Основные:\n" +
            ChatColor.BLACK + "/giveuranium <игрок> <предмет> [кол-во]\n" +
            "/radiation <игрок> <уровень>\n" +
            "/research start <тип>\n" +
            "/research status\n" +
            "/research craft <предмет>\n\n" +
            ChatColor.DARK_GREEN + "Центрифуга:\n" +
            ChatColor.BLACK + "/centrifuge create/start/status",
            
            // Страница 15 - Команды (продолжение)
            ChatColor.DARK_GREEN + "Лаборатория (админ):\n" +
            ChatColor.BLACK + "/laboratory authorize <игрок>\n" +
            "/laboratory unauthorize <игрок>\n" +
            "/laboratory list\n\n" +
            ChatColor.DARK_GREEN + "Гайд-бук:\n" +
            ChatColor.BLACK + "/uranium guide - получить эту книгу\n\n" +
            ChatColor.GRAY + "Большинство команд требуют соответствующих разрешений.",
            
            // Страница 16 - Рецепты
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "9. Рецепты\n\n" +
            ChatColor.DARK_GREEN + "Печь:\n" +
            ChatColor.BLACK + "Урановая руда → Урановый слиток\n\n" +
            ChatColor.DARK_GREEN + "Верстак:\n" +
            ChatColor.BLACK + "9 Урановых слитков + Редстоун → Обогащённый уран\n\n" +
            "Железо + Обогащённый уран → Топливный стержень\n\n" +
            "Урановый слиток + Кремень → Урановая пыль",
            
            // Страница 17 - Рецепты (продолжение)
            ChatColor.DARK_GREEN + "Детектор радиации:\n" +
            ChatColor.BLACK + "Редстоун + Компас + Железо\n\n" +
            ChatColor.DARK_GREEN + "Таблетки от радиации:\n" +
            ChatColor.BLACK + "Сахар + Молоко + Золотое яблоко\n\n" +
            ChatColor.DARK_GREEN + "Блок лаборатории:\n" +
            ChatColor.BLACK + "Железные блоки + Редстоун блоки + Маяк",
            
            // Страница 18 - Советы
            ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "10. Советы\n\n" +
            ChatColor.DARK_GREEN + "Безопасность:\n" +
            ChatColor.BLACK + "• Носите защитную броню\n" +
            "• Используйте детектор радиации\n" +
            "• Принимайте таблетки при отравлении\n\n" +
            ChatColor.DARK_GREEN + "Эффективность:\n" +
            ChatColor.BLACK + "• Стройте лаборатории для исследований\n" +
            "• Используйте центрифугу для получения пыли\n" +
            "• Изучайте продвинутые предметы",
            
            // Страница 19 - Дополнительные советы
            ChatColor.DARK_GREEN + "Продвинутые советы:\n" +
            ChatColor.BLACK + "• Урановые капсулы защищают от радиации при хранении\n" +
            "• Силовая броня имеет энергию - следите за уровнем\n" +
            "• Автошахтёр работает автоматически\n" +
            "• Рельсотрон - мощное оружие\n" +
            "• Электротранспорт экологичен\n\n" +
            ChatColor.GRAY + "Экспериментируйте с режимами!",
            
            // Страница 20 - Заключение
            ChatColor.GOLD + "" + ChatColor.BOLD + "Заключение\n\n" +
            ChatColor.BLACK + "Теперь вы знаете все основы UraniumCraft!\n\n" +
            "Помните о безопасности при работе с радиоактивными материалами.\n\n" +
            "Удачи в исследованиях!\n\n" +
            ChatColor.DARK_BLUE + "UraniumCraft Team\n" +
            ChatColor.GRAY + "Версия 2.0.0\n\n" +
            ChatColor.DARK_GRAY + "Для получения новой копии используйте /uranium guide"
        );
    }
}
