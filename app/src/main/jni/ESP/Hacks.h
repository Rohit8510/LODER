#ifndef ESP_HACKS_H
#define ESP_HACKS_H

#include "socket.h"
#include "Color.h"
#include "items.h"
#include "backends/ModsLoader.h"

int clrLineA, clrLineR, clrLineG, clrLineB;
int clrLineBotA, clrLineBotR, clrLineBotG, clrLineBotB;
Color clrEnemy, clrLine, clrLineBot, clrEdge, clrBox, clrAlert, clr, clrTeam, clrDist, clrHealth, clrText, grenadeColor, clrDistance;
float h, w, x, y, z, magic_number, mx, my, top, bottom, textsize, mScale, skelSize, LineSize;
Options options{1, -1, -1, 3, false, false, 1, false, 200, 200, 200, 19, 19, -1, false};
OtherFeature otherFeature{false, false, false, false};

int botCount, playerCount;
Response response;
Request request;
char extra[30];
char text[100];
int hCounter = 50;

Color colorByDistance (int distance, int alpha) {
    Color clrDistance;
    if (distance < 600)
        clrDistance = Color(0,180,0, alpha);
    if (distance < 300)
        clrDistance = Color(242,169,0, alpha);
    if (distance < 150)
        clrDistance = Color(255,0,0, alpha);
    return clrDistance;
}

bool isOutsideSafeZone (Vec2 pos, Vec2 screen) {
    if (pos.y < 0) return true;
    if (pos.x > screen.x) return true;
    if (pos.y > screen.y) return true;
    return pos.x < 0;
}

std::string playerstatus (int GetEnemyState) {
    switch (GetEnemyState) {
        case 520: case 544: case 656: case 521: case 528: case 3145736:
            return "Aiming";
        default:
            return "";
    }
}

Vec2 calculatePosition (const Vec2 &center, float radius, float angleDegrees) {
    float angleRadians = angleDegrees * (M_PI / 180.0f);
    float x = center.x + radius * cos(angleRadians);
    float y = center.y + radius * sin(angleRadians);
    return Vec2(x, y);
}

float getDisplayAngle(Vec2 position, Vec2 screen) {
    float centerX = screen.x / 2;
    float centerY = screen.y / 2;
    return atan2(position.y - centerY, position.x - centerX) * (180.0 / M_PI);
}

bool colorPosCenter (float sWidth, float smMx, float sHeight, float posT, float eWidth, float emMx, float eHeight, float posB) {
    return (sWidth >= smMx && sHeight >= posT && eWidth <= emMx && eHeight <= posB);
}

Vec2 pushToScreenBorder (const Vec2 &location, const Vec2 &screen, float offset, float scale = 2.0f) {
    Vec2 center(screen.x / 2, screen.y / 2);
    float angle = atan2(location.y - center.y, location.x - center.x) * (180.0f / M_PI);
    return calculatePosition(center, offset * scale, angle);
}

// ======================== RADAR (unchanged) ========================
void DrawRadar(ESP canvas, Vec2 Location, Vec2 Pos, float Size, Color clr, int TeamID) {
    float shiftX = 300.0f;
    float shiftY = 300.0f;
    float radarMinX = Pos.x - Size / 2 + shiftX;
    float radarMaxX = Pos.x + Size / 2 + shiftX;
    float radarMinY = Pos.y - Size / 2 + shiftY;
    float radarMaxY = Pos.y + Size / 2 + shiftY;
    Location.x = std::max(radarMinX, std::min(Location.x + shiftX, radarMaxX));
    Location.y = std::max(radarMinY, std::min(Location.y + shiftY, radarMaxY));
    canvas.DrawTransRoundRect(Color::White(30), {shiftX - Size, shiftY - Size},{shiftX + Size, shiftY + Size});
    canvas.DrawTransRoundRect(Color::White(255), {shiftX - Size/20, shiftY - Size/20},{shiftX + Size/20, shiftY + Size/20});
    canvas.DrawFillCircle(Color(clr.r, clr.g, clr.b, 255), Location, Size / 10, 0.5);
    if (isPlayerName) {
        canvas.DrawText(Color::White(255), std::to_string(TeamID).c_str(), Location, Size / 10);
    }
}

void DrawESP (ESP esp, int screenWidth, int screenHeight) {

    if (!g_Token.empty()) {
        if (!g_Auth.empty()) {
            if (g_Token == g_Auth) {

                esp.DrawTextName(Color::Red(255), "VERSION : 4.3", Vec2(screenWidth / 2, screenHeight - 25), screenHeight / 40);
                
                request.ScreenHeight = screenHeight;
                request.ScreenWidth = screenWidth;
                request.options = options;
                request.otherFeature = otherFeature;
                request.Mode = InitMode;

                botCount = 0, playerCount = 0;
                send((void *) &request, sizeof(request));
                receive((void *) &response);
                float mScaleY = screenHeight / (float) 1080;
                mScale = screenHeight / (float) 1080;
                mScale = screenHeight / (float) 1080;
                skelSize = (mScale * 1.5f);
                float BoxSize = (mScaleY * 2.0f);
                textsize = screenHeight / 50;
                Vec2 screen(screenWidth, screenHeight);
                
                if (response.Success) {
                    for (int i = 0; i < response.PlayerCount; i++) {
                        PlayerData Player = response.Players[i];
                        x = Player.HeadLocation.x;
                        y = Player.HeadLocation.y;
                        sprintf(extra, "%0.0fM", Player.Distance);
                        float magic_number = (response.Players[i].Distance * response.fov);
                        float namewidht = (screenWidth / 6) / magic_number;
                        float pp2 = namewidht / 2;
                        float mx = (screenWidth / 4) / magic_number;
                        float my = (screenWidth / 1.38) / magic_number;
                        float top = y - my + (screenWidth / 1.7) / magic_number;
                        float bottom = response.Players[i].Bone.lAn.y + my - (screenWidth / 1.7) / magic_number;
                        clrDist = colorByDistance((int) Player.Distance, 255);
                        clrAlert = _clrID((int) Player.TeamID, 80);
                        clrTeam = _clrID((int) Player.TeamID, 150);
                        clr = _clrID((int) Player.TeamID, 200);
                        Vec2 location(x, y);
                        float textsize = screenHeight / 50;
                        
                        bool isBot = response.Players[i].isBot;
                        bool isVisible = response.Players[i].isVisible;
                        if (isBot) {
                            botCount++;
                            if (isVisible) {
                                clr = Color(30, 232, 222, 255);  // Cyan - visible bot
                                clrEdge = Color(0, 255, 0, 255);  // Green edge
                            } else {
                                clr = Color(30, 232, 222, 180);  // Cyan with less alpha - invisible bot
                                clrEdge = Color(255, 0, 0, 255);  // Red edge
                            }
                            clrEnemy = Color::White(255);
                            clrLine = Color(clrLineBotR, clrLineBotG, clrLineBotB, clrLineBotA);
                            clrBox = Color::White(255);
                            clrText = Color::Black(255);
                        } else {
                            playerCount++;
                            if (isVisible) {
                                clr = Color(255, 0, 0, 255);  // RED - visible enemy
                                clrEdge = Color(0, 255, 0, 255);  // Green edge
                            } else {
                                clr = Color(255, 165, 0, 200);  // ORANGE - invisible enemy
                                clrEdge = Color(255, 0, 0, 255);  // Red edge
                            }
                            clrEnemy = clrDist;
                            clrLine = Color(clrLineR, clrLineG, clrLineB, clrLineA);
                            clrBox = Color::Orange(255);
                            clrText = Color::White(255);
                        }
                        
                        if (isRadar) {
                            DrawRadar(esp, Player.RadarLocation, request.radarPos,request.radarSize, clr, Player.TeamID);
                        }
                        
                        if (response.Players[i].HeadLocation.z != 1) { // On Screen
                            if (x > -50 && x < screenWidth + 50) {
                                if (isSkeleton && Player.Bone.isBone) {
                                    float skelSize = (mScaleY * 2.0f);
                                    float headsize = (mScaleY * 7.0f);
                                    float distanceFromCamera = Player.Distance;
                                    float minHeadSize = (mScaleY * 2.0f);
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.neck.x, response.Players[i].Bone.neck.y),Vec2(response.Players[i].Bone.cheast.x, response.Players[i].Bone.cheast.y));
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.cheast.x, response.Players[i].Bone.cheast.y),Vec2(response.Players[i].Bone.pelvis.x, response.Players[i].Bone.pelvis.y));
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.neck.x, response.Players[i].Bone.neck.y),Vec2(response.Players[i].Bone.lSh.x, response.Players[i].Bone.lSh.y));
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.neck.x, response.Players[i].Bone.neck.y),Vec2(response.Players[i].Bone.rSh.x, response.Players[i].Bone.rSh.y));
                                    esp.DrawLine(clr, skelSize, Vec2(response.Players[i].Bone.lSh.x, response.Players[i].Bone.lSh.y),Vec2(response.Players[i].Bone.lElb.x, response.Players[i].Bone.lElb.y));
                                    esp.DrawFilledCircle(clrEdge, Vec2(response.Players[i].Bone.lWr.x, response.Players[i].Bone.lWr.y),screenHeight / 20 / magic_number);
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.rSh.x, response.Players[i].Bone.rSh.y),Vec2(response.Players[i].Bone.rElb.x, response.Players[i].Bone.rElb.y));
                                    esp.DrawFilledCircle(clrEdge,Vec2(response.Players[i].Bone.rWr.x, response.Players[i].Bone.rWr.y),screenHeight / 20 / magic_number);
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.lElb.x, response.Players[i].Bone.lElb.y),Vec2(response.Players[i].Bone.lWr.x, response.Players[i].Bone.lWr.y));
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.rElb.x, response.Players[i].Bone.rElb.y),Vec2(response.Players[i].Bone.rWr.x, response.Players[i].Bone.rWr.y));
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.pelvis.x, response.Players[i].Bone.pelvis.y),Vec2(response.Players[i].Bone.lTh.x, response.Players[i].Bone.lTh.y));
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.pelvis.x, response.Players[i].Bone.pelvis.y),Vec2(response.Players[i].Bone.rTh.x, response.Players[i].Bone.rTh.y));
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.lTh.x, response.Players[i].Bone.lTh.y),Vec2(response.Players[i].Bone.lKn.x, response.Players[i].Bone.lKn.y));
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.rTh.x, response.Players[i].Bone.rTh.y),Vec2(response.Players[i].Bone.rKn.x, response.Players[i].Bone.rKn.y));
                                    esp.DrawFilledCircle(clrEdge,Vec2(response.Players[i].Bone.lAn.x, response.Players[i].Bone.lAn.y),screenHeight / 20 / magic_number);
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.lKn.x, response.Players[i].Bone.lKn.y),Vec2(response.Players[i].Bone.lAn.x, response.Players[i].Bone.lAn.y));
                                    esp.DrawLine(clr, skelSize,Vec2(response.Players[i].Bone.rKn.x, response.Players[i].Bone.rKn.y),Vec2(response.Players[i].Bone.rAn.x, response.Players[i].Bone.rAn.y));
                                    esp.DrawFilledCircle(clrEdge,Vec2(response.Players[i].Bone.rAn.x, response.Players[i].Bone.rAn.y),screenHeight / 20 / magic_number);
                                }

                                // Player Box
                                if (isPlayerBox) {
                                   esp.DrawLine(clrBox, BoxSize,Vec2(x + pp2, top),Vec2(x + namewidht, top));
                                   esp.DrawLine(clrBox, BoxSize,Vec2(x - pp2, top),Vec2(x - namewidht, top));
                                   esp.DrawLine(clrBox, BoxSize,Vec2(x + namewidht, top),Vec2(x + namewidht, top + pp2));
                                   esp.DrawLine(clrBox, BoxSize,Vec2(x - namewidht, top),Vec2(x - namewidht, top + pp2));
                                   // bottom
                                   esp.DrawLine(clrBox, BoxSize,Vec2(x + pp2, bottom),Vec2(x + namewidht, bottom));
                                   esp.DrawLine(clrBox, BoxSize,Vec2(x - pp2, bottom),Vec2(x - namewidht, bottom));
                                   esp.DrawLine(clrBox, BoxSize,Vec2(x - namewidht, bottom),Vec2(x - namewidht, bottom - pp2));
                                    esp.DrawLine(clrBox, BoxSize,Vec2(x + namewidht, bottom),Vec2(x + namewidht, bottom - pp2));
                                }

                                if (isPlayerLine){
                                    float LineSize = (mScaleY * 3.0f);
                                    esp.DrawLine(clr, LineSize,Vec2(screenWidth / 2, screenHeight / 12),Vec2(x, top - screenHeight / 32));
                                }
                                
                                if (isPlayerHealth) {
                                    float healthLength = screenWidth / 60;
                                    if (healthLength < mx)
                                        healthLength = mx;

                                    if (Player.Health < 25)
                                        clrHealth = Color(255, 0, 0);
                                    else if (Player.Health < 50)
                                        clrHealth = Color(255, 165, 0);
                                    else if (Player.Health < 75)
                                        clrHealth = Color(255, 255, 0);
                                    else
                                        clrHealth = Color(0, 255, 0);
                                    
                                    if (Player.Health == 0) {
                                        esp.DrawText(Color(255, 0, 0), "Knocked", Vec2(x, top - screenHeight / 200), textsize);
                                    } else {
                                        esp.DrawFilledRect(clrHealth,Vec2(x - healthLength, top - screenHeight / 100),Vec2(x - healthLength + (2 * healthLength) * Player.Health / 100, top - screenHeight / 200));
                                        esp.DrawRect(Color(0, 0, 0), screenHeight / 660,Vec2(x - healthLength, top - screenHeight / 100),Vec2(x + healthLength, top - screenHeight / 200));
                                    }
                                }
                                
                                if (isPlayerName && response.Players[i].isBot) {
                                    if (response.Players[i].Health > 0) {
                                        esp.DrawText(Color().White(255), "ROBOT",Vec2(x + 5.5, top - screenHeight / 28),textsize);
                                    } else if (response.Players[i].Health == 0) {
                                        esp.DrawText(Color().Red(255), "KNOCK OUT",Vec2(x, top - screenHeight / 28), textsize);
                                    }
                                } else if (isPlayerName) {
                                    if (response.Players[i].Health > 0) {
                                        std::string playerName = (char*)response.Players[i].PlayerNameByte;
                                        esp.DrawName(Color(255, 255, 255), playerName.c_str(),Vec2(x + 5.5, top - screenHeight / 28),screenHeight / 53);
                                    } else if (response.Players[i].Health == 0) {
                                        esp.DrawText(Color().Red(255), "KNOCK OUT",Vec2(x, top - screenHeight / 28), textsize);
                                    }

                                }
                                
                               /* if (isPlayerTeamID && response.Players[i].Health > 0) {
                                    char teamStr[16];
                                    sprintf(teamStr, "ID:%d", response.Players[i].TeamID);
                                    esp.DrawText(Color(255, 255, 0), teamStr, Vec2(x - screenWidth / 33.5, top - screenHeight / 28), screenHeight / 53);
                                }*/
                                
                                if (isPlayerNation) {
                                    if (response.Players[i].Health <= 0) {
                                    } else {
                                        if (response.Players[i].isBot) {
                                            esp.DrawNation(Color(255, 255, 255, 255),response.Players[i].PlayerNation,Vec2(x - -10, top - -7), 28);
                                        } else {
                                            esp.DrawNation(Color(255, 255, 255, 255),response.Players[i].PlayerNation,Vec2(x - -10, top - -7), 28);
                                        }
                                    }
                                }

                                // UID
                                if (isPlayerUID) {
                                   // esp.DrawUserID(Color().Orange(255),response.Players[i].PlayerUID, Vec2(response.Players[i].HeadLocation.x - 25,top - screenHeight / 15), screenHeight / 60);
                                }
                                
                                //Player Head
                                if (isPlayerHead){
                                   esp.DrawFilledCircle(clrEdge,Vec2(response.Players[i].HeadLocation.x,response.Players[i].HeadLocation.y),screenHeight / 12 / magic_number);
                                }
                                
                                if (isPlayerDistance && response.Players[i].Health > 0) {
                                    sprintf(extra, "%0.0f m", response.Players[i].Distance);
                                    esp.DrawText(Color(255, 180, 0), extra, Vec2(x, bottom + screenHeight / 45), textsize);
                                }
                                
                                // weapon text only
                                if (isPlayerWeapon && response.Players[i].Weapon.isWeapon && response.Players[i].Health > 0) {
                                    esp.DrawWeapon(Color(247, 244, 200), response.Players[i].Weapon.id, response.Players[i].Weapon.ammo, response.Players[i].Weapon.ammo, Vec2(x, bottom + screenHeight / 23), textsize);
                                }
                                
                                if (Player.isVisible) {
                                    if (playerstatus(Player.StatusPlayer) == "Aiming") {
                                        esp.DrawTexture(Color::Yellow(255)," ⚠\uFE0F Player Aiming at you ⚠\uFE0F",Vec2(screenWidth / 2, screenHeight / 4.3),screenHeight / 30);
                                    }
                                }
                            } //OnScreen

                            if (is360Alert) {
                                if (response.Players[i].HeadLocation.z == 1.0f) {
                                    if (x > screenWidth - screenWidth / 12)
                                        x = screenWidth - screenWidth / 120;
                                    else if (x < screenWidth / 120)
                                        x = screenWidth / 12;
                                    if (y < screenHeight / 1) {
                                        esp.DrawFilledCircle(Color(255, 0, 0, 80), Vec2(0, y),screenHeight / 18);
                                        sprintf(extra, "%0.0f m", response.Players[i].Distance);
                                        esp.DrawText(Color(180, 250, 181, 200), extra,Vec2(screenWidth / 80, y), textsize);
                                    } else {
                                        esp.DrawFilledCircle(Color(255, 0, 0, 80), Vec2(0, y),screenHeight / 18);
                                        sprintf(extra, "%0.0f m", response.Players[i].Distance);
                                        esp.DrawText(Color(180, 250, 181, 200), extra,Vec2(screenWidth / 80, y), textsize);
                                    }
                                } else if (x < -screenWidth / 10 || x > screenWidth + screenWidth / 10) {
                                    if (y > screenHeight - screenHeight / 12)
                                        y = screenHeight - screenHeight / 120;
                                    else if (y < screenHeight / 120) y = screenHeight / 12;
                                    if (x > screenWidth / 2) {
                                        esp.DrawFilledCircle(Color(255, 0, 0, 80), Vec2(0, y),screenHeight / 18);
                                        sprintf(extra, "%0.0f m", response.Players[i].Distance);
                                        esp.DrawText(Color(180, 250, 181, 200), extra,Vec2(screenWidth / 80, y), textsize);
                                    } else {
                                        esp.DrawFilledCircle(Color(255, 0, 0, 80), Vec2(0, y),screenHeight / 18);
                                        sprintf(extra, "%0.0f m", response.Players[i].Distance);
                                        esp.DrawText(Color(180, 250, 181, 200), extra,Vec2(screenWidth / 80, y), textsize);
                                    }
                                } else if (y < -screenHeight / 10 || y > screenHeight + screenHeight / 10) {
                                    if (x > screenWidth - screenWidth / 12)
                                        x = screenWidth - screenWidth / 120;
                                    else if (x < screenWidth / 120)
                                        x = screenWidth / 12;
                                    if (y > screenHeight / 2.5) {
                                        esp.DrawFilledCircle(Color(255, 0, 0, 80), Vec2(screenWidth, y),screenHeight / 18);
                                        sprintf(extra, "%0.0f m", response.Players[i].Distance);
                                        esp.DrawText(Color(180, 250, 181, 200), extra,Vec2(screenWidth - screenWidth / 80, y), textsize);
                                    } else {
                                        esp.DrawFilledCircle(Color(255, 0, 0, 80), Vec2(0, y),screenHeight / 18);
                                        sprintf(extra, "%0.0f m", response.Players[i].Distance);
                                        esp.DrawText(Color(180, 250, 181, 200), extra,Vec2(screenWidth / 80, y), textsize);
                                    }

                                }

                                if (isOutsideSafeZone(location, screen)) {
                                    Vec2 hintDotRenderPos = pushToScreenBorder(location, screen,(mScaleY * 100) / 2,5.0f);
                                    float angle = getDisplayAngle(hintDotRenderPos, screen);
                                    if (response.Players[i].isBot) {
                                        esp.DrawTriangle(Color::Green(255), hintDotRenderPos,(mScaleY * 20), angle);
                                    } else {
                                        esp.DrawTriangle(Color::Red(255), hintDotRenderPos,(mScaleY * 20), angle);
                                    }
                                }
                            }
                        } //Player.HeadLocation.z
                    } //response.PlayerCount
                    
                    for (int i = 0; i < response.GrenadeCount; i++) {
                        GrenadeData grenade = response.Grenade[i];
                        if (!isGrenadeWarning || grenade.Location.z == 1.0f) continue;
                        const char *grenadeTypeText;
                        switch (grenade.type) {
                            case 1: grenadeColor = Color::Red(255); grenadeTypeText = "Grenade"; break;
                            case 2: grenadeColor = Color::Orange(255); grenadeTypeText = "Molotov"; break;
                            case 3: grenadeColor = Color::Yellow(255); grenadeTypeText = "Stun"; break;
                            default: grenadeColor = Color::White(255); grenadeTypeText = "Smoke";
                       }
                       sprintf(extra, "%s (%0.0f m)", grenadeTypeText, grenade.Distance);
                       sprintf(text, "Throwable %s (%0.0f m)", grenadeTypeText, grenade.Distance);
                       esp.DrawText(grenadeColor, extra, Vec2(grenade.Location.x, grenade.Location.y + (screenHeight / 50)), textsize);
                       esp.DrawTexture(Color::White(255), text, Vec2(screenWidth / 2 + screenHeight / 245, screenHeight / 5.0), screenHeight / 45);
                       esp.DrawText(grenadeColor, "〇", Vec2(grenade.Location.x, grenade.Location.y), textsize);
                    } //response.GrenadeCount
                    
                    for (int i = 0; i < response.VehicleCount; i++) {
                        if (isVehicles) {
                            VehicleData vehicle = response.Vehicles[i];
                            if (vehicle.Location.z != 1.0f) {
                                esp.DrawVehicles(vehicle.VehicleName, vehicle.Distance,vehicle.Health, vehicle.Fuel,Vec2(vehicle.Location.x, vehicle.Location.y),screenHeight / 47);
                            }
                        }
                    } //response.VehicleCount

                    for (int i = 0; i < response.ItemsCount; i++) {
                        if (isItems) {
                            ItemData currentItem = response.Items[i];
                            if (currentItem.Location.z != 1.0f) {
                                esp.DrawItems(currentItem.ItemName, currentItem.Distance,Vec2(currentItem.Location.x, currentItem.Location.y),screenHeight / 50);
                            }
                        }
                    } //response.ItemsCount
                } //response.Success
                
                if (botCount + playerCount > 0) {
                    esp.DrawFilledRect(Color(40, 40, 40, 200),Vec2(screenWidth / 2 - screenHeight / 15, screenHeight / 18),Vec2(screenWidth / 2 + screenHeight / 15, screenHeight / 10.5)); // Background color - dark grey
                    sprintf(extra, "%d", playerCount);
                    esp.DrawText(Color(255, 0, 0), extra,Vec2(screenWidth / 2 - screenHeight / 25, screenHeight / 10.8),screenHeight / 27);
                    sprintf(extra, "%d", botCount);
                    esp.DrawText(Color(30, 232, 222), extra,Vec2(screenWidth / 2 + screenHeight / 40, screenHeight / 10.8),screenHeight / 27);
                    esp.DrawLine(Color(0, 0, 0), 3, Vec2(screenWidth / 2, screenHeight / 18),Vec2(screenWidth / 2, screenHeight / 10.5));
                } else {
                    esp.DrawFilledRect(Color(40, 40, 40, 200), Vec2(screenWidth / 2 - screenHeight / 15, screenHeight / 18), Vec2(screenWidth / 2 + screenHeight / 15, screenHeight / 10.5));
                    esp.DrawText(Color(255, 255, 255, 255), "CLEAR", Vec2(screenWidth / 2, screenHeight / 11), screenHeight / 35);
                }
                
                if (options.tracingStatus) {
                    float py = screenHeight / 2;
                    float px = screenWidth / 2;
                    esp.DrawFilledRect(Color::Green(50),Vec2(options.touchY - options.touchSize / 2,py * 2 - options.touchX + options.touchSize / 2),Vec2(options.touchY + options.touchSize / 2,py * 2 - options.touchX - options.touchSize / 2));
                }

                if (options.openState == 0 || options.aimBullet == 0 || options.aimT == 0) {
                    const Color textColor = (options.openState == 0) ? Color::Red(255) : (options.aimT == 0 ? Color::Blue(255) : Color::Green(255));
                    esp.DrawCircle(textColor, Vec2(screenWidth / 2, screenHeight / 2),options.aimingRange, 1.5);
                }

                if (isLootItems) {
                    for (int i = 0; i < response.BoxItemsCount; i++) {
                        if (response.BoxItems[i].Location.z != 1.0f) {
                            BoxItemData *boxData = &response.BoxItems[i];
                            char *itemname;
                            int BoxCount = 0;
                            for (int ij = 0; ij < boxData->itemCount; ij++) {
                                if (GetBox((int) boxData->itemID[ij], &itemname)) {
                                    BoxCount++;
                                    esp.DrawDeadBoxItems(Color(), itemname,Vec2(boxData->Location.x,boxData->Location.y -(float) BoxCount *(screenHeight / 50)),textsize);
                                }
                            }
                        }
                    }
                }
            } //g_Token == g_Auth
        } //g_Auth
    } //g_Token
} //OnDraw

#endif //ESP_HACKS_H