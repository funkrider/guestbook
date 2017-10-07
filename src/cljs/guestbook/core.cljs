(ns guestbook.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST]]))

(defn message-list2 [messages]
  [:ul.content
   (for [{:keys [timestamp message name]} @messages]
     ^{:key timestamp}
     [:li
      [:time (.toLocaleString timestamp)]
      [:p message]
      [:p " - " name]])])

(defn get-messages2 [messages]
  (GET "/messages"
       {:headers {"Accept" "application/transit+json"}
        :handler #(reset! messages (vec %))}))

(defn send-message!2 [fields errors messages]
  (POST "/message"
        {:headers {"Accept"       "application/transit+json"
                   "x-csrf-token" (.-value (.getElementById js/document "token"))}
         :params @fields
         :handler #(do
                    (reset! errors nil)
                    (swap! messages conj (assoc @fields :timestamp (js/Date.))))
         :error-handler #(do
                          (.log js/console (str %))
                          (reset! errors (get-in % [:response :errors])))}))

(defn errors-component2 [errors id]
  (when-let [error (id @errors)]
    [:div.alert.alert-danger (clojure.string/join error)]))

(defn message-form2 [messages]
  (let [fields (atom {})
        errors (atom nil)]
    (fn []
      [:div.content
       [errors-component2 errors :server-error]
       [:div.form-group
        [errors-component2 errors :name]
        [:p "Name:"
         [:input.form-control
          {:type      :text
           :name      :name
           :on-change #(swap! fields assoc :name (-> % .-target .-value))
           :value     (:name @fields)}]]
        [errors-component2 errors :message]
        [:p "Message:"
         [:textarea.form-control
          {:rows      4
           :cols      50
           :name      :message
           :value     (:message @fields)
           :on-change #(swap! fields assoc :message (-> % .-target .-value))}]]
        [:input.btn.btn-primary
         {:type     :submit
          :on-click #(send-message!2 fields errors messages)
          :value    "comment"}]]])))



(defn home2 []
  (let [messages (atom nil)]
    (get-messages2 messages) 
    (fn []
      [:div
       [:div.row
        [:div.span12
         [message-list2 messages]]]
       [:div.row
        [:div.span12
         [message-form2 messages]]]])))

;; ---------------------------------------------------------------------------------------------------


(defn send-message! [fields]
  (POST "/message"
        {:format :json
         :headers {"Accept" "application/transit+json" "x-csrf-token" (.-value (.getElementById js/document "token"))}
         :params @fields
          :handler #(.log js/console (str "response: " %))
          :error-handler #(.log js/console (str "error: " %))}))


(defn message-form [messages]
  (let [fields (atom {})]
    (fn []
      [:div.content
       [:div.form-group
        [:p "Name:" (:name @fields)
         [:input.form-control
          {:type      :text
           :name      :name
           :on-change #(swap! fields assoc :name (-> % .-target .-value))
           :value     (:name @fields)}]]
        [:p "Message:" (:message @fields)
         [:textarea.form-control
          {:rows      4
           :cols      50
           :name      :message
           :value     (:message @fields)
           :on-change #(swap! fields assoc :message (-> % .-target .-value))}]]
        [:input.btn.btn-primary
         {:type     :submit
          :on-click #(send-message! fields)
          :value    "comment"}]]])))

(defn home []
  [:div.row
   [:div.span12
    [message-form]]])

(reagent/render
  [home]
  (.getElementById js/document "content"))
