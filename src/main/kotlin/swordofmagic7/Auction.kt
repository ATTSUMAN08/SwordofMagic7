package swordofmagic7

import org.bukkit.entity.Player
import swordofmagic7.Data.PlayerData
import swordofmagic7.Inventory.ItemParameterStack
import swordofmagic7.MultiThread.runTask
import swordofmagic7.Sound.SoundList
import swordofmagic7.TextView.TextView
import java.util.*

import static swordofmagic7.Data.PlayerData.playerData
import static net.somrpg.swordofmagic7.SomCore.instance
import static swordofmagic7.Sound.CustomSound.playSound

object Auction {
    var stack: ItemParameterStack? = null
    var Owner: PlayerData? = null
    var Better: PlayerData? = null
    var time = 0
    var Mel = 0
    var Auctioning = false

    fun auctionCommand(playerData: PlayerData, args: Array<String>) {
        val player = playerData.player
        if (!player.hasPermission("som7.auction.ban")) {
            if (args.size >= 2) {
                try {
                    val type = args[0]
                    val index = args[1].toInt()
                    if (index > -1 && type.equals("sell", ignoreCase = true) && playerData.ItemInventory.list.size > index) {
                        if (!Auctioning) {
                            stack = playerData.ItemInventory.getItemParameterStack(index).clone()
                            if (args.size >= 3) {
                                stack!!.amount = args[2].toInt()
                            }
                            if (!playerData.ItemInventory.hasItemParameter(stack) || stack!!.amount < 1) {
                                Function.sendMessage(player, "§e所持数§a以上は§b出品§aできません", SoundList.Nope)
                                return
                            }
                            if (args.size >= 4) {
                                Mel = args[3].toInt()
                            } else Mel = 1
                            if (Mel < 1) Mel = 1
                            val StartMel = Mel
                            val reqMel = Math.ceil(Mel * 0.01).toInt()
                            if (playerData.Mel < reqMel) {
                                Function.sendMessage(player, "§e出品手数料§aが足りません §e[$reqMel]", SoundList.Nope)
                                return
                            } else {
                                Function.sendMessage(player, "§c[出品手数料]§e$reqMelメル")
                            }
                            Owner = playerData
                            Auctioning = true
                            runTask({
                                var text = TextView(Owner!!.nick + "§aさんが")
                                text.addView(stack!!.itemParameter.getTextView(stack!!.amount, Owner!!.viewFormat()))
                                text.addText("§aを§eオークション§aに§e$Melメル§aから§b出品§aしました")
                                Client.sendDisplay(Owner!!.player, text)
                                time = 30
                                var error: String? = null
                                while (0 < time && instance.isEnabled) {
                                    val list: MutableList<String> = mutableListOf()
                                    list.add(Function.decoText("オークション"))
                                    list.add(Function.decoLore("§b§l出品者") + Owner!!.nick)
                                    list.add(Function.decoLore("§b§l出品物") + "§e§l" + stack!!.itemParameter.display + "§a§lx" + stack!!.amount)
                                    if (Better != null) {
                                        list.add(Function.decoLore("§b§l入札額") + Mel + "メル")
                                        list.add(Function.decoLore("§b§l入札者") + Better!!.nick)
                                    } else {
                                        list.add(Function.decoLore("§b§l入札額") + "§7§l未入札")
                                        list.add(Function.decoLore("§b§l入札者") + "§7§l未入札")
                                    }
                                    list.add(Function.decoLore("§b§l残り時間") + time + "秒")
                                    for (loopPlayer in PlayerList.PlayerList) {
                                        if (loopPlayer.isOnline) {
                                            playerData(loopPlayer).viewBar.setSideBar("Auction", list)
                                        }
                                    }
                                    time--
                                    MultiThread.sleepTick(20)
                                    if (!Owner!!.ItemInventory.hasItemParameter(stack)) {
                                        error = "§e出品者§aが§e出品物§aを§c紛失§aしたため§eオークション§aを終了します"
                                        break
                                    } else if (!Owner!!.player.isOnline) {
                                        error = "§e出品者§aが§c失踪§aしたため§eオークション§aを終了します"
                                        break
                                    } else if (Better != null && Better!!.Mel < Mel) {
                                        Better = null
                                        Mel = StartMel
                                        Function.BroadCast("§e入札者§aの§eメル残高§a§e入札額§a下回ったため§eオークション§aの§b入札§aを取り消します", SoundList.Tick)
                                    } else if (Better != null && !Better!!.player.isOnline) {
                                        Better = null
                                        Mel = StartMel
                                        Function.BroadCast("§e入札者§aの§c失踪§aしたため§eオークション§aの§b入札§aを取り消します", SoundList.Tick)
                                    }
                                }
                                if (error != null) {
                                    Function.BroadCast(error, SoundList.Tick)
                                } else if (Better != null) {
                                    val reqMel2 = Math.ceil(Mel * 0.05).toInt()
                                    text = TextView(Better!!.nick + "§aさんが")
                                    text.addView(stack!!.itemParameter.getTextView(stack!!.amount, Owner!!.viewFormat()))
                                    text.addText("§aを§e$Melメル§aで§c落札§aしました")
                                    Client.sendDisplay(Better!!.player, text)
                                    Better!!.ItemInventory.addItemParameter(stack)
                                    Owner!!.ItemInventory.removeItemParameter(stack)
                                    Better!!.Mel -= Mel
                                    Owner!!.Mel += Mel - reqMel2
                                    Function.sendMessage(player, "§c[取引手数料]§e$reqMel2メル")
                                    Better!!.viewUpdate()
                                    Owner!!.viewUpdate()
                                } else {
                                    Function.BroadCast("§e入札者§aが現れなかったため§eオークション§aが§c終了§aしました", SoundList.Tick)
                                }
                                for (loopPlayer in PlayerList.PlayerList) {
                                    if (loopPlayer.isOnline) {
                                        playerData(loopPlayer).viewBar.resetSideBar("Auction")
                                    }
                                }
                                Owner = null
                                Better = null
                                Auctioning = false
                            }, "Auction")
                        } else {
                            Function.sendMessage(player, "§aすでに§eオークション§aが開催されています", SoundList.Nope)
                        }
                        return
                    } else if (type.equals("bet", ignoreCase = true)) {
                        if (Function.CheckBlockPlayer(playerData, Owner)) return
                        if (Auctioning) {
                            if (playerData !== Owner) {
                                val reqMel = Math.ceil(Mel * 1.05f).toInt()
                                if (reqMel <= index) {
                                    if (playerData.Mel >= index) {
                                        Better = playerData
                                        Mel = index
                                        Function.BroadCast(Better!!.nick + "§aさんが§e" + index + "メル§aで§b入札§aしました", SoundList.Tick)
                                        if (time < 10) time = 10
                                    } else {
                                        Function.sendMessage(player, "§eメル§aが足りません", SoundList.Nope)
                                    }
                                } else {
                                    Function.sendMessage(player, "§e$reqMelメル§a以上でないと§b入札§a出来ません", SoundList.Nope)
                                }
                            } else {
                                player.sendMessage("§a自身の§eオークション§aには§e入札§a出来ません")
                                playSound(player, SoundList.Nope)
                            }
                        } else {
                            player.sendMessage("§eオークション§aが開催されていません")
                            playSound(player, SoundList.Nope)
                        }
                        return
                    }
                } catch (ignore: Exception) {
                }
            }
            player.sendMessage(Function.decoLore("/auction sell <SlotID> [<個数>] [<開始金額>]"))
            player.sendMessage(Function.decoLore("/auction bet <メル>"))
        } else {
            player.sendMessage("§cあなたはオークションの利用が制限されています")
            playSound(player, SoundList.Nope)
        }
    }
}
