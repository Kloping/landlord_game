package com.hrs.kloping;

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @version 0.1
 * @Author HRS 3474006766@qq.com
 * @Date 21/9/14
 * <p>
 * 请把images.zip 解压后放在 Mirai Console 目录下
 */
public final class HPlugin_Landlord extends JavaPlugin {
    public static final HPlugin_Landlord INSTANCE = new HPlugin_Landlord();
    /**
     * 换成你的QQ 用来强制关闭游戏
     */
    public static final Long youQQ = Utils.init("#这里写上你的QQ 用来强制关闭游戏", "host", 3474006766L, Long.class);

    public static final ExecutorService threads = Executors.newFixedThreadPool(10);

    private HPlugin_Landlord() {
        super(new JvmPluginDescriptionBuilder("com.hrs.kloping.h_plugin_Landlord", "0.1")
                .name("插件_1 Author => HRS")
                .info("斗地主游戏")
                .author("HRS")
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("HRS's Plugin loaded!");
        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost() {
            @Override
            public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
                super.handleException(context, exception);
            }

            @EventHandler
            public void handleMessage(GroupMessageEvent event) {
                String text = event.getMessage().get(1).toString();
                if (Command.isCommand(text)) {
                    Command.exec(text.substring(1), event.getSender().getId(), event.getGroup());
                }
            }
        });
    }
}