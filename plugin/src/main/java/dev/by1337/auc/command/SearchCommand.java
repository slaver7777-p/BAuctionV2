package dev.by1337.auc.command;

import dev.by1337.auc.command.args.SearchArgument;
import dev.by1337.auc.config.Config;
import dev.by1337.auc.menu.HomeMenu;
import dev.by1337.auc.search.SearchManager;
import dev.by1337.auc.search.filter.SearchFilter;
import dev.by1337.auc.util.ByLocaleSelector;
import dev.by1337.bmenu.BMenu;
import dev.by1337.cmd.Command;
import dev.by1337.core.util.misc.Pair;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SearchCommand extends Command<CommandSender> {

    private static final Logger log = LoggerFactory.getLogger(SearchCommand.class);
    private static final ByLocaleSelector<SearchManager> selector = new ByLocaleSelector<>("ru_RU");

    public SearchCommand(String name, Map<String, SearchManager> search, Config config) {
        super(name);
        requires(s -> s instanceof Player);
        search.values().forEach(v -> selector.add(v.locale(), v));
        executor(
                new SearchArgument<>("filter", selector, config),
                this::run
        );
    }

    private void run(CommandSender sender, Pair<String, SearchFilter> pair) {
        if (!(sender instanceof Player player)) return;
        if (pair != null) {
            open(player, pair.getLeft(), pair.getRight());
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) {
            handItem = player.getInventory().getItemInOffHand();
        }

        if (handItem.getType() == Material.AIR) {
            BMenu.menuLoader().create("bauc:home", player, null).open();
            return;
        }

        final SearchManager manager = selector.select(player.locale());
        if (manager == null) {
            log.error("has no SearchManager for locale {}", player.locale());
            return;
        }

        final Pair<String, SearchFilter> pair1 = manager.filterForItem(handItem.getType());
        if (pair1 == null) {
            BMenu.menuLoader().create("bauc:home", player, null).open();
            return;
        }

        open(player, pair1.getLeft(), pair1.getRight());
    }

    private void open(Player player, String input, SearchFilter filter) {
        var menu = BMenu.menuLoader().create("bauc:home", player, null);
        if (menu instanceof HomeMenu h) {
            h.setSearch(filter);
            h.setSearchInput(input);
        }
        menu.open();
    }
}
